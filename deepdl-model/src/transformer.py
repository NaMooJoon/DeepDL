'''
This module is based on the implemenation of transformer from tensorflow. 
https://www.tensorflow.org/text/tutorials/transformer?hl=ko
Few features are modified for the convenience.
'''

import tensorflow as tf
import numpy as np

def get_angles(pos, i, d_model):
  angle_rates = 1 / np.power(10000, (2 * (i // 2)) / np.float32(d_model))
  
  return pos * angle_rates


def positional_encoding(position, d_model):
  angle_rads = get_angles(np.arange(position)[:, np.newaxis],
                          np.arange(d_model)[np.newaxis, :],
                          d_model)

  # apply sin to even indices in the array; 2i
  angle_rads[:, 0::2] = np.sin(angle_rads[:, 0::2])

  # apply cos to odd indices in the array; 2i+1
  angle_rads[:, 1::2] = np.cos(angle_rads[:, 1::2])

  pos_encoding = angle_rads[np.newaxis, ...]

  return tf.cast(pos_encoding, dtype=tf.float32)


def point_wise_feed_forward_network(d_model, dff):
  return tf.keras.Sequential([
      tf.keras.layers.Dense(dff, activation='relu'),  # (batch_size, seq_len, dff)
      tf.keras.layers.Dense(d_model)])  # (batch_size, seq_len, d_model)


class EncoderLayer(tf.keras.layers.Layer):
  
  def __init__(self, d_model, num_heads, dff, rate=0.1):
    super(EncoderLayer, self).__init__()

    self.mha = tf.keras.layers.MultiHeadAttention(num_heads, 
                                                  d_model // num_heads)  # modified: using tf.keras.MultiHeadAttention
    self.ffn = point_wise_feed_forward_network(d_model, dff)

    self.layernorm1 = tf.keras.layers.LayerNormalization(epsilon=1e-6)
    self.layernorm2 = tf.keras.layers.LayerNormalization(epsilon=1e-6)

    self.dropout1 = tf.keras.layers.Dropout(rate)
    self.dropout2 = tf.keras.layers.Dropout(rate)

  def call(self, x, training, mask):
    attn_out = self.mha(x, x, x, mask, 
                        return_attention_scores=False, 
                        training=training, 
                        use_causal_mask=False)  # (batch_size, input_seq_len, d_model)
    attn_out = self.dropout1(attn_out, training=training)
    out1 = self.layernorm1(x + attn_out)  # (batch_size, input_seq_len, d_model)

    ffn_out = self.ffn(out1)  # (batch_size, input_seq_len, d_model)
    ffn_out = self.dropout2(ffn_out, training=training)
    out2 = self.layernorm2(out1 + ffn_out)  # (batch_size, input_seq_len, d_model)

    return out2


class DecoderLayer(tf.keras.layers.Layer):
  
  def __init__(self, d_model, num_heads, dff, rate=0.1):
    super(DecoderLayer, self).__init__()

    self.mha1 = tf.keras.layers.MultiHeadAttention(num_heads, 
                                                   d_model // num_heads) # modified: using tf.keras.MultiHeadAttention
    self.mha2 = tf.keras.layers.MultiHeadAttention(num_heads, 
                                                   d_model // num_heads) # modified: using tf.keras.MultiHeadAttention

    self.ffn = point_wise_feed_forward_network(d_model, dff)

    self.layernorm1 = tf.keras.layers.LayerNormalization(epsilon=1e-6)
    self.layernorm2 = tf.keras.layers.LayerNormalization(epsilon=1e-6)
    self.layernorm3 = tf.keras.layers.LayerNormalization(epsilon=1e-6)

    self.dropout1 = tf.keras.layers.Dropout(rate)
    self.dropout2 = tf.keras.layers.Dropout(rate)
    self.dropout3 = tf.keras.layers.Dropout(rate)

  def call(self, x, enc_output, training, enc_mask, dec_mask):  # modified: remove param look_ahead_mask
    # enc_output.shape == (batch_size, input_seq_len, d_model)

    # modified: calling tf.keras.MultiHeadAttention
    attn1, attn_weights_block1 = self.mha1(x, x, x, dec_mask,                            
                                           return_attention_scores=True, 
                                           training=training, 
                                           use_causal_mask=True)  # (batch_size, target_seq_len, d_model) 
    attn1 = self.dropout1(attn1, training=training)
    out1 = self.layernorm1(attn1 + x)

    # modified: calling tf.keras.MultiHeadAttention
    attn2, attn_weights_block2 = self.mha2(out1, enc_output, enc_output, 
                                           enc_mask, 
                                           return_attention_scores=True, 
                                           training=training, 
                                           use_causal_mask=False)  # (batch_size, target_seq_len, d_model)
    attn2 = self.dropout2(attn2, training=training)
    out2 = self.layernorm2(attn2 + out1)  # (batch_size, target_seq_len, d_model)

    ffn_out = self.ffn(out2)  # (batch_size, target_seq_len, d_model)
    ffn_out = self.dropout3(ffn_out, training=training)
    out3 = self.layernorm3(ffn_out + out2)  # (batch_size, target_seq_len, d_model)

    return out3, attn_weights_block1, attn_weights_block2
  
  
class Encoder(tf.keras.layers.Layer):
  
  def __init__(self, num_layers, d_model, num_heads, dff, input_vocab_size,
               maximum_position_encoding, rate=0.1):
    super(Encoder, self).__init__()

    self.d_model = d_model
    self.num_layers = num_layers

    self.embedding = tf.keras.layers.Embedding(input_vocab_size, d_model)
    self.pos_encoding = positional_encoding(maximum_position_encoding,
                                            self.d_model)

    self.enc_layers = [EncoderLayer(d_model, num_heads, dff, rate)
                       for _ in range(num_layers)]

    self.dropout = tf.keras.layers.Dropout(rate)

  def call(self, x, training, mask):

    seq_len = tf.shape(x)[1]

    # adding embedding and position encoding.
    x = self.embedding(x)  # (batch_size, input_seq_len, d_model)
    x *= tf.math.sqrt(tf.cast(self.d_model, tf.float32))
    x += self.pos_encoding[:, :seq_len, :]

    x = self.dropout(x, training=training)

    for i in range(self.num_layers):
      x = self.enc_layers[i](x, training, mask)

    return x  # (batch_size, input_seq_len, d_model)  


class Decoder(tf.keras.layers.Layer):
  
  def __init__(self, num_layers, d_model, num_heads, dff, target_vocab_size,
               maximum_position_encoding, rate=0.1):
    super(Decoder, self).__init__()

    self.d_model = d_model
    self.num_layers = num_layers

    self.embedding = tf.keras.layers.Embedding(target_vocab_size, d_model)
    self.pos_encoding = positional_encoding(maximum_position_encoding, d_model)

    self.dec_layers = [DecoderLayer(d_model, num_heads, dff, rate)
                       for _ in range(num_layers)]
    self.dropout = tf.keras.layers.Dropout(rate)

  def call(self, x, enc_output, training, enc_mask, dec_mask): # modified: remove param look_ahead_mask
    seq_len = tf.shape(x)[1]
    attention_weights = {}

    x = self.embedding(x)  # (batch_size, target_seq_len, d_model)
    x *= tf.math.sqrt(tf.cast(self.d_model, tf.float32))
    x += self.pos_encoding[:, :seq_len, :]

    x = self.dropout(x, training=training)

    for i in range(self.num_layers):
      x, block1, block2 = self.dec_layers[i](x, enc_output, training, 
                                             enc_mask, dec_mask) # modified: remove arg look_ahead_mask

      attention_weights[f'decoder_layer{i+1}_block1'] = block1
      attention_weights[f'decoder_layer{i+1}_block2'] = block2

    # x.shape == (batch_size, target_seq_len, d_model)
    return x, attention_weights 
  
  
class TransformerAccuracy(tf.keras.metrics.Metric):
  
  def __init__(self, name=None, dtype=None, **kwargs):
    super(TransformerAccuracy, self).__init__(name, dtype, **kwargs)
    
    self.num_correct = self.add_weight('num_correct', 
                                       dtype=tf.float32, initializer='zeros')
    self.total = self.add_weight('total', 
                                 dtype=tf.float32, initializer='zeros')
    
  def update_state(self, y_true, y_pred, **kwargs):
    accuracies = tf.equal(tf.argmax(y_pred, axis=2, output_type=tf.int32), 
                          y_true)
    mask = tf.math.logical_not(tf.math.equal(y_true, 0))
    
    accuracies = tf.cast(tf.math.logical_and(accuracies, mask), 
                         dtype=tf.float32)
    mask = tf.cast(mask, dtype=tf.float32)
    
    self.num_correct.assign_add(tf.reduce_sum(accuracies))
    self.total.assign_add(tf.reduce_sum(mask))
  
  def result(self):
    return tf.math.divide(self.num_correct, self.total)