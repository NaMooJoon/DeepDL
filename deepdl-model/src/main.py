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

import tensorflow as tf

from deepdl import DeepDLConfig, DeepDLTransformer, DeepDL
from utils import getpd

EPOCHS = 50
BATCH_SIZE = 16
CEN_SEQ_LEN = 160
CON_SEQ_LEN = 4 * CEN_SEQ_LEN

def main(argv: list) -> None:
    if len(argv) < 4 or (argv[1] == '-ts' and len(argv) < 5):
        print('usage: python main.py <vocab-size>',
              '<path-to-train-data-file> <path-to-test-data-file>')
        print('       python main.py <vocab-size>', 
              '-tr <path-to-data-file>')
        print('       python main.py <vocab-size>', 
              '-ts <path-to-model-weight-file>', '<path-to-data-file>')

        return
    
    if argv[2] == '-tr':
        train(int(argv[1]), os.path.normpath(argv[3]), BATCH_SIZE, EPOCHS)
    
     
def train(vocab_size: int, fn: str, batch_size : int, epochs: int) -> None:    
    rn = fn.split(os.sep)[-2]
    dn = os.path.join(getpd(), 'out', rn)
    
    if not os.path.exists(dn):
        os.mkdir(dn)
        
    cen_line, con_line_block, _ = load_data(fn)
    cen_enc_in, con_enc_in, dec_in, dec_out = preprocess(cen_line, 
                                                         con_line_block)
    config = DeepDLConfig(rn, len(cen_enc_in), batch_size)
    model = DeepDLTransformer(vocab_size)
    
    model.compile(optimizer=config.optimizer, loss=config.loss, 
                  metrics=[config.accuracy])
    model.fit(x=[cen_enc_in, con_enc_in, dec_in], y=dec_out, 
              batch_size=batch_size, epochs=epochs, 
              callbacks=[config.model_checkpoint],
              validation_split=0.1)

def load_data(fn: str) -> tuple:
    cen_line = []
    con_line_block = []
    label = []
	
    with open(fn, newline='') as f:
        reader = csv.reader(f)
        
        next(reader)
    
        for row in reader:
            cen_line.append(json.loads(row[0]))
            con_line_block.append(json.loads(row[1]))  
            
            if len(row) > 2:
                label.append(row[2] == 'True')

    return cen_line, con_line_block, label

def preprocess(cen_line: list, con_line_block: list) -> tuple:
    eol = cen_line[0][-1]
    sos = con_line_block[0][0]
    eos = con_line_block[0][-1]
    
    for i in range(len(cen_line)):
        if len(cen_line[i]) > CEN_SEQ_LEN or len(con_line_block[i]) > CON_SEQ_LEN:
            cen_line.pop(i)
            con_line_block.pop(i)
        else:
            for j in range(CEN_SEQ_LEN - len(cen_line[i])):
                cen_line[i].append(0)

            for j in range(CON_SEQ_LEN - len(con_line_block[i])):
                con_line_block[i].append(0)
                
    cen_enc_in = tf.constant(cen_line)
    con_enc_in = tf.constant(con_line_block)
    
    for line in cen_line:
        idx = line.index(eol)
        
        line.pop(idx)
        line.insert(idx, eos)
    
    dec_out = tf.constant(cen_line)
    
    for line in cen_line:
        idx = line.index(eos)
        
        line.pop(idx)
        line.insert(0, sos)
    
    dec_in = tf.constant(cen_line)
    
    return cen_enc_in, con_enc_in, dec_in, dec_out
	     
def test(vocab_size: int, w_fn: str, d_fn: str) -> None:
    pass


if __name__ == '__main__':
    main(sys.argv)



