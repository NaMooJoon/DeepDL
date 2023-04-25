import tensorflow as tf
import numpy as np

from operator import itemgetter
from transformer import Encoder, Decoder, TransformerAccuracy

class DeepDLConfig():
  def __init__(self):
    learning_rate = tf.keras.optimizers.schedules.ExponentialDecay(0.1, 
                                                                   4000, 0.99)
    self.optimizer = tf.keras.optimizers.legacy.SGD(learning_rate=learning_rate, 
                                              momentum=0.5)
    self.optimizer.clipnorm = 5
    self.loss = DeepDLLoss()
    self.accuracy = TransformerAccuracy()
    self.model_checkpoint = tf.keras.callbacks.ModelCheckpoint(
        os.path.join(os.getcwd(), 
                     "..", "out", "weigths.{epcoh:02d}-{val_loss:.2f}.hdf5"),
        verbose=1, 
        save_best_only=True,
        save_weights_only=True)


class DeepDLTransformer(tf.keras.Model):
  
  def __init__(self, vocab_size, num_layers=6, d_model=512, num_heads=8, 
               dff=2048, pe_cent=160, pe_contxt=640, rate=0.1):
    super(DeepDLTransformer, self).__init__()
    
    self.central_encoder = Encoder(num_layers, d_model, num_heads, dff,
                                   vocab_size, pe_cent, rate)
    self.contextual_encoder = Encoder(num_layers, d_model, num_heads, dff,
                                      vocab_size, pe_contxt, rate)
    self.attention_layer = tf.keras.layers.MultiHeadAttention(
            num_heads, d_model // num_heads)
    self.decoder = Decoder(num_layers, d_model, num_heads, dff,
                           vocab_size, pe_cent, rate)
    self.linear_layer = tf.keras.layers.Dense(target_vocab_size)

  def call(self, inputs, training, use_attn_out=False):
    cen_enc_in, con_enc_in, dec_in = inputs
    
    if not use_attn_out: 
      self.cen_enc_padding_mask = self.create_padding_mask(cen_enc_in)
      con_enc_padding_mask = self.create_padding_mask(con_enc_in) 
      
      cen_enc_out = self.central_encoder(cen_enc_in, 
                                         training, self.cen_enc_padding_mask)  # (batch_size, cen_in_seq_len, d_model)
      con_enc_out = self.contextual_encoder(con_enc_in, 
                                            training, con_enc_padding_mask)  # (batch_size, con_in_seq_len, d_model)
      
      self.attn_out = self.attention_layer(cen_enc_out, con_enc_out, 
                                          con_enc_padding_mask, False, 
                                          training, False)  # (batchsize, cen_in_seq_len, d_model) 
  
    dec_out, attn_w_dict = self.decoder(dec_in, self.attn_out, 
                                        training, self.cen_enc_padding_mask)  # dec_output.shape == (batch_size, dec_in_seq_len, d_model)
    linear_out = self.linear_layer(dec_out)  # (batch_size, dec_in_seq_len, target_vocab_size)
    out = tf.math.softmax(linear_out, axis=2)

    return out, attn_w_dict

  def create_padding_mask(seq):
    seq = tf.cast(tf.math.logical_not(tf.math.equal(seq, 0)), dtype=tf.float32)

    # add extra dimensions to add the padding
    # to the attention logits.
    return seq[:, tf.newaxis, tf.newaxis, :]  # (batch_size, 1, 1, seq_len)
  

class DeepDL(tf.Module):
   
  def __init__(self, model, start_id, end_id):
    self.model = model
    self.start_id = start_id
    self.end_id = end_id
    
  def __call__(self, cent_line, contxt_line_block):
    res = []
    
    for i in range(cent_line.shape[0]):
      res.append(calculate(cent_line[i, :], contxt_line_block[i, :]))  

    res.sort(key=itemgetter(2), reverse=True)
    
    return res
  
  def calculate(self, cent_line, contxt_line_block):
    dec_in = tf.constant([[self.start_id]])
    entropy = 0.0
    
    while True:
      out = self.model([cent_line, contxt_line_block, dec_in], False, 
                        False if i == 0 else True)
      last_seq_id = tf.math.argmax(out[0, -1, :], axis=2)
      
      if last_seq_id[0][0] == self.end_id:
        break
      
      entropy -= np.log2(out[0][-1][last_seq_id[0][0]])
      dec_in = tf.concat([dec_in, last_seq_id], axis=1)  
    
    return cent_line[0], dec_in[1:], entropy
  
  
class DeepDLLoss(tf.keras.losses.Loss):
  
  def __init__(self, name=None):
    super(TransformerLoss, self).__init__('none', name)
  
  def call(self, y_true, y_pred):
    loss = tf.keras.losses.SparseCategoricalCrossentropy(
        reduction='none')(y_true, y_pred)
    mask = tf.cast(tf.math.logical_not(tf.math.equal(y_true, 0)), 
                   dtype=loss.dtype) 
    
    return tf.math.divide(tf.reduce_sum(loss * mask), y_true.shape[0])