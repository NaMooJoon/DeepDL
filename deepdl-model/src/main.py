'''
Hyper parameters based on the Qiu et al. - Deep Just-In-Time Defect Localization

epochs: 50
batch size: 16
optimizer: SGD
initial learning rate: 0.1 -> 0.01
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
from utils import PlotType, getpd, convert_to_dataframe, plot

EPOCHS = 50
BATCH_SIZE = 16
CEN_SEQ_LEN = 160
CON_SEQ_LEN = 4 * CEN_SEQ_LEN
DUMMY_DATA = tf.constant([[0]]) 

def main(argv: list) -> None:    
    if argv[2] == '-tr' and len(argv) >= 4:
        if len(argv) == 4:
            train(int(argv[1]), argv[3], BATCH_SIZE, EPOCHS)
        else:
            train(int(argv[1]), argv[3], BATCH_SIZE, EPOCHS, argv[4])
    elif argv[2] == '-ts' and len(argv) >= 5:
        if len(argv) == 5:
            test(int(argv[1]), argv[3], argv[4])
        else:
            test(int(argv[1]), argv[3], argv[4], argv[5])
    elif argv[2] == '-a' and len(argv) >= 5:
        if len(argv) == 5:
            applicate(int(argv[1]), argv[3], argv[4])
        else:
            applicate(int(argv[1]), argv[3], argv[4], argv[5])
    else:
        print('usage: python main.py <vocab-size>', 
              '-tr <path-to-train-data-file>', 
              '<model-weight-output-file-path>')
        print('       python main.py <vocab-size>', 
              '-ts <path-to-model-weight-file>' '<path-to-test-data-file>',
              '<plot-output-file-path>')
        print('       python main.py <vocab-size>', 
              '-a <path-to-model-weight-file> <path-to-application-data-file>',
              '<ranking-output-file-path>')

     
def train(vocab_size: int, d_fn: str, batch_size : int, epochs: int, 
          o_fp: str = None) -> None:    
    if o_fp == None:
        o_fp = os.path.join(getpd(), 'out', 'weights', d_fn.split(os.sep)[-2])

    os.makedirs(o_fp, exist_ok=True)
    
    cen_lines, con_line_blocks, _ = load_data(d_fn)
    
    with tf.distribute.MirroredStrategy().scope():
        cen_enc_in, con_enc_in, dec_in, dec_out = preprocess(cen_lines, 
                                                             con_line_blocks)
        config = DeepDLConfig(o_fp, len(cen_enc_in), batch_size)
        model = DeepDLTransformer(vocab_size)
        
        model.compile(optimizer=config.optimizer, loss=config.loss, 
                      metrics=[config.accuracy])
        model.fit(x=[cen_enc_in, con_enc_in, dec_in], y=dec_out, 
                  batch_size=batch_size, epochs=epochs, 
                  callbacks=[config.model_checkpoint],
                  validation_split=0.1)
   
    
def load_data(fn: str) -> tuple:
    cen_lines = []
    con_line_blocks = []
    labels = []
	
    with open(fn, newline='') as f:
        reader = csv.reader(f)
        
        next(reader)
    
        for row in reader:
            cen_lines.append(json.loads(row[0]))
            con_line_blocks.append(json.loads(row[1]))  
            labels.append(row[2] == 'true')

    return cen_lines, con_line_blocks, labels


def preprocess(cen_lines: list, con_line_blocks: list) -> tuple:
    eol = cen_lines[0][-1]
    sos = con_line_blocks[0][0]
    eos = con_line_blocks[0][-1]
    padded_cen_lines = []
    padded_con_line_blocks = []
    
    for i in range(len(cen_lines)):
        if (len(cen_lines[i]) <= CEN_SEQ_LEN 
                and len(con_line_blocks[i]) <= CON_SEQ_LEN):
            cen_line = cen_lines[i][:]
            con_line_block = con_line_blocks[i][:]
            
            for j in range(CEN_SEQ_LEN - len(cen_line)):
                cen_line.append(0)
            
            padded_cen_lines.append(cen_line)

            for j in range(CON_SEQ_LEN - len(con_line_block)):
                con_line_block.append(0)
             
            padded_con_line_blocks.append(con_line_block)    
                
    cen_enc_in = tf.constant(padded_cen_lines)
    con_enc_in = tf.constant(padded_con_line_blocks)
    
    for line in padded_cen_lines:
        idx = line.index(eol)
        
        line.pop(idx)
        line.insert(idx, eos)
    
    dec_out = tf.constant(padded_cen_lines)
    
    for line in padded_cen_lines:
        idx = line.index(eos)
        
        line.pop(idx)
        line.insert(0, sos)
    
    dec_in = tf.constant(padded_cen_lines)
    
    return cen_enc_in, con_enc_in, dec_in, dec_out
	
      
def test(vocab_size: int, w_fn: str, d_dn: str, o_fp: str = None) -> None:
    '''
    Tests the trained model of the given weight file 
    with the given vocabulary size and all of the test data  
    in the given test data directory. 
    Top-k accuracy, MAP, MRR of the trained model is printed
    and plots are saved to the given output file path. 
    If the output file path is none, 
    the plots are saved in out/plots/repository.  
    
    Args:
        vocab_size (int): The vocabulary size.
        w_fn (str): The model weight file name.
        d_dn (str): The test data directory name.
        o_fp (str, optional): The output file path.
    '''
    
    list_cen_lines = []
    list_con_line_blocks = []
    list_labels = []
    
    if o_fp == None: 
        o_fp = os.path.join(getpd(), 'out', 'plots', w_fn.split(os.sep)[-2])
        
    os.makedirs(o_fp, exist_ok=True)
    
    for fn in os.listdir(d_dn):
        cen_lines, con_line_blocks, labels = load_data(os.path.join(d_dn, fn))
        list_cen_lines.append(cen_lines)
        list_con_line_blocks.append(con_line_blocks)
        list_labels.append(labels)
    
    sos = list_con_line_blocks[0][0][0]
    eos = list_con_line_blocks[0][0][-1]        
    cen_line = list_cen_lines[0][0].copy()
    
    cen_line.insert(0, sos)
    cen_line.pop()
    
    with tf.distribute.MultiWorkerMirroredStrategy().scope():
        model = DeepDLTransformer(vocab_size)
        
        model([tf.constant([list_cen_lines[0][0]]), 
            tf.constant([list_con_line_blocks[0][0]]), 
            tf.constant([cen_line])], training=False)
        model.load_weights(w_fn)
        
        top1_acc, top5_acc, mrr, map_, predictions = DeepDL(model, sos, eos).evaluate(
                [list_cen_lines, list_con_line_blocks], list_labels)

        print(f'top1 accuracy: {top1_acc}')
        print(f'top5 accuracy: {top5_acc}')
        print(f'MRR: {mrr}')
        print(f'MAP: {map_}')

    df = convert_to_dataframe(predictions, list_labels)
    
    ax = plot(df, PlotType.LABEL_ENTROPY)
    
    ax.get_figure().savefig(os.path.join(o_fp, 'LABEL_ENTROPY.png'))
    ax.clear()
    
    ax = plot(df, PlotType.LABEL_LENGTH)
    
    ax.get_figure().savefig(os.path.join(o_fp, 'LABEL_LENGTH.png'))
    ax.clear()
    
    grid = plot(df, PlotType.LABEL_LENGTH_ENTROPY)
    
    grid.savefig(os.path.join(o_fp, 'LABEL_LENGTH_ENTROPY.png'))
    grid.clear()

 
def applicate(vocab_size: int, w_fn: str, d_fn: str, o_fp: str = None) -> None:
    '''
    Applicates the DeepDL to the given data file 
    with the given vocabulary size and weight file, 
    and saves the ranking csv file in the given output file path.
    If the output file path is None, 
    the ranking csv file is saved in out/rankings/repository.
    
    Args:
        vocab_size (int): The vocabulary size.
        w_fn (str): The weight file name.
        d_fn (str): The data file name.
        o_fp (str, optional): The output file path.
    '''
    
    path_elements = d_fn.split(os.sep)
    hash = path_elements[-1].split('.')[0]
    
    if o_fp == None:
        o_fp = os.path.join(getpd(), 'out', 'rankings', path_elements[-2])
    
    os.makedirs(o_fp, exist_ok=True)
    
    cen_lines, con_line_blocks, _ = load_data(d_fn)
    sos = con_line_blocks[0][0]
    eos = con_line_blocks[0][-1]
    
    with tf.distribute.MultiWorkerMirroredStrategy().scope():
        model = DeepDLTransformer(vocab_size)
     
        model([DUMMY_DATA, DUMMY_DATA, DUMMY_DATA], training=False)
        model.load_weights(w_fn)
        
        out = DeepDL(model, sos, eos)([cen_lines, con_line_blocks])
    
    with open(os.path.join(o_fp, 'raw_ranking_' + hash + '.txt'),
              mode='w', newline='') as f:
        writer = csv.writer(f)
        
        writer.writerow(['ID', 'CentralLine', 
                         'GeneratedLine', 'Entropy'])       
        writer.writerows(out)
        
    
if __name__ == '__main__':
    main(sys.argv)


