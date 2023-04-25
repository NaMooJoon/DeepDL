'''
Hyper parameters based on the Qiu et al. - Deep Just-In-Time Defect Localization

epochs: 50
batch size: 16
optimizer: SGD
initial learning rate: 0.1
learning rate decay: 0.99
momentum: 0.5
clip gradients norm: 5
loss function: cross entropy loss
validation set: 10% of the training set
saving frequency: every epoch
final model selection: lowest entropy score 
'''

import csv
import json
import os
import sys

from deepdl import DeepDLConfig, DeepDLTransformer, DeepDL

import tensorflow as tf

EPOCHS = 50
BATCH_SIZE = 16
CEN_SEQ_LEN = 160
CONTXT_SEQ_LEN = 4 * CEN_SEQ_LEN

def main(argv: list):
    if len(argv) < 4 or (argv[1] == '-ts' and len(argv) < 5):
        print('usage: python main.py <vocab-size>',
              '<path-to-train-data-file> <path-to-test-data-file>')
        print('       python main.py <vocab-size>', 
              '-tr <path-to-data-file>')
        print('       python main.py <vocab-size>', 
              '-ts <path-to-model-weight-file>', '<path-to-data-file>')

        return
    if argv[2] == '-tr':
        train(int(argv[1]), argv[2], BATCH_SIZE, EPOCHS)
    
    
    
def train(vocab_size: int, fn: str, batch_size : int, epochs: int) -> None:
    config = DeepDLConfig()
    cent_line, contxt_line_block, _ = load_data(fn)
    cen_enc_in, con_enc_in, dec_in, dec_out = preprocess(cent_line, 
                                                         contxt_line_block)
    model = DeepDLTransformer(vocab_size)
    
    model.compile(optimizer=config.optimizer, loss=config.loss, 
                  metrics=[config.accuracy])
    model.fit(x=[cen_enc_in, con_enc_in, dec_in], y=dec_out, 
              batch_size=batch_size, epochs=epochs, 
              callbacks=[config.model_checkpoint],
              validation_split=0.1)

def load_data(fn: str) -> tuple:
    cent_line = []
    contxt_line_block = []
    label = []
	
    with open(fn, newline='') as f:
        reader = csv.reader(f)
        
        next(reader)
    
        for row in reader:
            cent_line.append(json.loads(row[0]))
            contxt_line_block.append(json.loads(row[1]))  
            
            if len(row) > 2:
                label.append(row[2] == 'True')

    return cent_line, contxt_line_block, label

def preprocess(cent_line: list, contxt_line_block: list) -> tuple:
    eol = cent_line[0][-1]
    sos = contxt_line_block[0][0]
    eos = contxt_line_block[0][-1]
    
    for line in cent_line:
        for i in range(CEN_SEQ_LEN - len(line)):
            line.append(0)
    
    cen_enc_in = tf.constant(cent_line)
    
    for line in contxt_line_block:
        for i in range(CONTXT_SEQ_LEN - len(line)):
            line.append(0)	
    
    contxt_enc_in = tf.constant(contxt_lince_block)
    
    for line in cent_line:
        idx = line.index(eol)
        
        line.pop(idx)
        line.insert(idx, eos)
    
    dec_out = tf.constant(cent_line)
    
    for line in cent_line:
        idx = line.index(eos)
        
        line.pop(idx)
        line.insert(0, sos)
    
    dec_in = tf.constant(cent_line)
    
    return cen_enc_in, contxt_enc_in, dec_in, dec_out
	     
def test(vocab_size: int, w_fn: str, d_fn: str) -> None:
    pass

if __name__ == '__main__':
    main(sys.argv)


