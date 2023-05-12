import os

import numpy as np
import tensorflow as tf

from operator import itemgetter
from transformer import Encoder, Decoder, TransformerAccuracy
from utils import getpd, topk, RR, AP

class DeepDLConfig():
  
  def __init__(self, rn, num_data, batch_size):
    learning_rate = tf.keras.optimizers.schedules.ExponentialDecay(
        0.01, num_data // batch_size, 0.99)
    self.__optimizer = tf.keras.optimizers.SGD(learning_rate=learning_rate, 
                                               momentum=0.5)
    self.__optimizer.clipnorm = 5
    self.__loss = DeepDLLoss()
    self.__accuracy = TransformerAccuracy()
    self.__model_checkpoint = tf.keras.callbacks.ModelCheckpoint(
        os.path.join(getpd(), 
                     "out", rn, "weights.{epoch:02d}-{val_loss:.2f}.hdf5"),
        verbose=1, 
        save_best_only=True,
        save_weights_only=True)

  @property
  def optimizer(self):
    return self.__optimizer
  
  @property
  def loss(self):
    return self.__loss
  
  @property
  def accuracy(self):
    return self.__accuracy
  
  @property
  def model_checkpoint(self):
    return self.__model_checkpoint


class DeepDLTransformer(tf.keras.Model):
  
  def __init__(self, vocab_size, num_layers=6, d_model=512, num_heads=8, 
               dff=2048, pe_cen_enc=160, pe_con_enc=640, pe_dec=160, rate=0.1):
    super(DeepDLTransformer, self).__init__()
    
    self.__pe_dec = pe_dec
    self.central_encoder = Encoder(num_layers, d_model, num_heads, dff,
                                   vocab_size, pe_cen_enc, rate)
    self.contextual_encoder = Encoder(num_layers, d_model, num_heads, dff,
                                      vocab_size, pe_con_enc, rate)
    self.attention_layer = tf.keras.layers.MultiHeadAttention(
        num_heads, d_model // num_heads)
    self.decoder = Decoder(num_layers, d_model, num_heads, dff,
                           vocab_size, pe_dec, rate)
    self.linear_layer = tf.keras.layers.Dense(vocab_size)

  def call(self, inputs, training=None, mask=None, use_attn_out=False):
    cen_enc_in, con_enc_in, dec_in = inputs
    dec_padding_mask = self.create_padding_mask(dec_in)
    
    if not use_attn_out: 
      self.cen_enc_padding_mask = self.create_padding_mask(cen_enc_in)
      con_enc_padding_mask = self.create_padding_mask(con_enc_in) 
      
      cen_enc_out = self.central_encoder(cen_enc_in, 
                                         training, self.cen_enc_padding_mask)  # (batch_size, cen_in_seq_len, d_model)
      con_enc_out = self.contextual_encoder(con_enc_in, 
                                            training, con_enc_padding_mask)  # (batch_size, con_in_seq_len, d_model)
      
      self.attn_out = self.attention_layer(cen_enc_out, con_enc_out, 
                                           attention_mask=con_enc_padding_mask, 
                                           return_attention_scores=False, 
                                           training=training, 
                                           use_causal_mask=False)  # (batchsize, cen_in_seq_len, d_model) 
    
    dec_out, attn_w_dict = self.decoder(dec_in, self.attn_out, 
                                        training, 
                                        self.cen_enc_padding_mask,
                                        dec_padding_mask)  # dec_output.shape == (batch_size, dec_in_seq_len, d_model)
    linear_out = self.linear_layer(dec_out)  # (batch_size, dec_in_seq_len, target_vocab_size)
    out = tf.math.softmax(linear_out, axis=2)

    return out if training else (out, attn_w_dict)

  def create_padding_mask(self, seq):
    seq = tf.cast(tf.math.logical_not(tf.math.equal(seq, 0)), dtype=tf.float32)

    # add extra dimensions to add the padding
    # to the attention logits.
    return seq[:, tf.newaxis, tf.newaxis, :]  # (batch_size, 1, 1, seq_len)
  
  @property
  def pe_dec(self):
    return self.__pe_dec
  

class DeepDL(tf.Module):
   
  def __init__(self, model, start_id, end_id):
    self.model = model
    self.start_id = start_id
    self.end_id = end_id
    
  def __call__(self, X):
    '''
    Calls DeepDL with the given data.
    
    Args: 
      x (list): The data that consists of tokenized central lines 
                and contexutal line blocks that padding does not added. 
                            
    Returns:
      list: Calculated results that consists tuples of index number, 
            central line, generated tokens, and line entropy.
            The calculated results are sorted by the line entropy 
            in descending order.          
    '''
    
    cen_lines, con_line_blocks = X
    res = []
    
    for i in range(len(cen_lines)):
      res.append((i, ) + self._calculate([cen_lines[i]], [con_line_blocks[i]]))  

    res.sort(key=itemgetter(3), reverse=True)
    
    return res
  
  def _calculate(self, cen_line, con_line_block):
    '''
    Generates code line and calculates the line entropy with 
    the given tokenized central line and contextaul line block.
    
    Args:
      cen_line (list): The tokenized central line.
                       It expects to receive 2D central line that
                       padding does not added.
      con_line_block (list): The tokenized contextual line block.
                             It expects to receive 
                             2D contexutal line block that 
                             padding does not added.
        
    Retruns:
      tuple: Calculated result that consists of central line, 
             the generated tokens, and the line entropy.
    '''
    
    cen_enc_in = tf.constant(cen_line)
    con_enc_in = tf.constant(con_line_block)
    dec_in = tf.constant([[self.start_id]])
    entropy = 0.0
    
    for i in range(self.model.pe_dec):
      out, _ = self.model([cen_enc_in, con_enc_in, dec_in], 
                          training=False, 
                          use_attn_out=False if i == 0 else True)
      last_seq_id = tf.math.argmax(out[:, -1:, :], axis=2, output_type=tf.int32)
      
      if last_seq_id[0][0] == self.end_id:
        break
      
      entropy -= np.log(out[0][-1][last_seq_id[0][0]])
      dec_in = tf.concat([dec_in, last_seq_id], axis=1)  
    
    return (cen_line[0], dec_in[0][1:].numpy().tolist(), 
            entropy if dec_in.shape[1] == 1 
                    else entropy / (dec_in.shape[1] - 1))
  
  def evaluate(self, X, Y):
    '''
    Evaluates this module with the given commit data and labels.
    The evaluation metrics are top-k accuracy, MRR, and MAP.
    
    Args: 
      x (list): The commit data. 
                It expects list that consists of list of central lines 
                and the list of contextual line blocks 
                which are both divided by commits without padding.   
      y (list): The labels which are divided by commits.
      
    Returns:
      tuple: The evaluation of this module 
             which consists of top-1 accuracy, top-5 accuracy, MRR and MAP.
      None: None if labels of some commit do not contain True value.
    '''
    
    try:
      cen_lines, con_line_blocks = X
      total_top1 = 0.0
      total_top5 = 0.0
      total_rr = 0.0
      total_ap = 0.0

      for i in range(len(cen_lines)):
        out = self([cen_lines[i], con_line_blocks[i]])
        total_top1 += topk(out, Y[i], 1)
        total_top5 += topk(out, Y[i], 5)
        total_rr += RR(out, Y[i])
        total_ap += AP(out, Y[i])
    
      return (total_top1 / len(cen_lines), total_top5 / len(cen_lines), 
              total_rr / len(cen_lines), total_ap / len(cen_lines))
    except Exception as e:
      print(e)
      
      return None
    
    
class DeepDLLoss(tf.keras.losses.Loss):
  
  def __init__(self, name=None):
    super(DeepDLLoss, self).__init__('none', name)
  
  def call(self, y_true, y_pred):
    loss = tf.keras.losses.SparseCategoricalCrossentropy(
        reduction='none')(y_true, y_pred)
    mask = tf.cast(tf.math.logical_not(tf.math.equal(y_true, 0)), 
                   dtype=loss.dtype) 
    
    return tf.math.divide(tf.reduce_sum(loss * mask), 
                          tf.cast(tf.shape(y_true)[0], tf.float32))