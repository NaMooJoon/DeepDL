import tensorflow as tf

from transformer import Encoder, Decoder

num_layers = 6
d_model = 512
num_heads = 8
dff = 2048

learning_rate = tf.keras.optimzers.schedule.ExponentialDecay(0.1, 4000, 0.99)
optimizer = tf.optimizers.legacy.SGD(learning_rate, 0.5)
optimizer.clipnorm = 5
loss = tf.losses.SparseCategoricalCrossentropy(reduction='sum_over_batch_size')
metrics = tf.metrics.SparseCategoricalAccuracy()


class DeepDLTransformer(tf.keras.Model):
  def __init__(self, num_layers, d_model, num_heads, dff, input_vocab_size,
               target_vocab_size, pe_input, pe_target, rate=0.1):
    super(DeepDLTransformer, self).__init__()
    
    self.central_encoder = Encoder(num_layers, d_model, num_heads, dff,
                                   input_vocab_size, pe_input, rate)
    self.context_encoder = Encoder(num_layers, d_model, num_heads, dff,
                                   input_vocab_size, pe_input, rate)
    self.attention_layer = tf.keras.layers.MultiHeadAttention(num_heads, 
                                                              d_model // num_heads)
    self.decoder = Decoder(num_layers, d_model, num_heads, dff,
                           target_vocab_size, pe_target, rate)
    self.linear_layer = tf.keras.layers.Dense(target_vocab_size)

  def call(self, inputs, training, mask=None):
    cen_enc_in, con_enc_in, dec_in = inputs
    cen_enc_padding_mask = self.create_padding_mask(cen_enc_in)
    con_enc_padding_mask = self.create_padding_mask(con_enc_in) 
    
    cen_enc_out = self.central_encoder(cen_enc_in, 
                                       training, cen_enc_padding_mask)  # (batch_size, cen_in_seq_len, d_model)
    con_enc_out = self.context_encoder(con_enc_in, 
                                       training, con_enc_padding_mask)  # (batch_size, con_in_seq_len, d_model)
    
    attn_out = self.attention_layer(cen_enc_out, con_enc_out, 
                                    con_enc_padding_mask, False, 
                                    training, False)  # (batchsize, cen_in_seq_len, d_model) 
    dec_out, attn_w_dict = self.decoder(dec_in, attn_out, 
                                        training, cen_enc_padding_mask)  # dec_output.shape == (batch_size, dec_in_seq_len, d_model)
    linear_out = self.linear_layer(dec_out)  # (batch_size, dec_in_seq_len, target_vocab_size)
    out = tf.math.softmax(linear_out, axis=2)

    return out, attn_w_dict

  def create_padding_mask(seq):
    seq = tf.cast(tf.math.logical_not(tf.math.equal(seq, 0)), dtype=tf.float32)

    # add extra dimensions to add the padding
    # to the attention logits.
    return seq[:, tf.newaxis, tf.newaxis, :]  # (batch_size, 1, 1, seq_len)