import os

import matplotlib.pyplot as plt
import seaborn as sns

from enum import Enum
from matplotlib.pyplot import Axes
from pandas import DataFrame
from seaborn import JointGrid
from typing import Union

PROJECT_NAME = 'deepdl-model'

class PlotType(Enum):
    LABEL_ENTROPY = 0
    LABEL_LENGTH = 1
    LABEL_LENGTH_ENTROPY = 2


def getpd() -> str:
    path = __file__.split(os.sep)
    
    for element in reversed(path):
        if element == PROJECT_NAME:
            break
        else:
            path.pop()
    
    return os.sep.join(path)    


def topk(y_pred: list, y_true: list, k: int) -> int:
    '''
    Checks whether the given predictions contains at least one buggy line 
    within the given kth line with the given labels.
  
    Args:
      	y_pred (list): The predictions that consist of tuples of index number, 
                       central line, generated tokens, and line entropy.
      	y_true (list): The labels.  
      	k (int): Constant that indicates the number of lines to check.
    
    Returns:
     	int: 1 if the predictions contain at least one buggy line 
           	 within the kth line, 0 otherwise.
      
    Raises:
    	Exception: Exception raises when the labels do not contain True.
    '''

    if not True in y_true:
        raise Exception('invalid labels')
    
    for i in range(len(y_pred)):
        if i == k:
            break
        
        if y_true[y_pred[i][0]]:
            return 1
    
    return 0


def RR(y_pred, y_true):
    '''
    Gets reciprocal rank of the given predictions with the given labels.
    
    Args:
      	y_pred (list): The predictions that consist of tuples of index number, 
                       central line, generated tokens, and line entropy.
      	y_true (list): The labels.
    
    Returns: 
      	float: Reciprocal rank of the predictions 
    
    Raises:
      	Exception: Exception rasies when the labels do not contain True.        
    '''
    
    if not True in y_true:
        raise Exception('invalid labels')
    
    for i in range(len(y_pred)):
    	if y_true[y_pred[i][0]]:
    		return 1.0 / (i + 1)
    
    
def AP(y_pred, y_true):
    '''
    Caculates average precision of the given predictions with the given labels.
    
    Args:
      	y_pred (list): The predictions that consist of tuples of index number,
                       central line, generated tokens, and line entropy.
      	y_true (list): The labels.
    
    Returns: 
      	float: Average precision of the predictions.
    
    Raises:
      	Exception: Exception rasies when the labels do not contain True.  
    '''
    
    if not True in y_true:
        raise Exception('invalid labels')
    
    total_precision = 0.0
    n_true = 0
    
    for i in range(len(y_pred)):
        if y_true[y_pred[i][0]]:
            n_true += 1
            total_precision += n_true / (i + 1)
    
    return total_precision / n_true
  
  
def convert_to_dataframe(Y_pred: list, Y_true: list) -> DataFrame:
	'''
	Converts the given predictions into DataFrame with the given labels.
	The column of the DataFrame is "Label", "Length", and "Entropy". 	
 
	Args:
		Y_pred (list): The data.
					   It expects list of list of tuples 
      				   that consists of index number, central line, 
           			   generated tokens, and line entorpy.
		Y_true (list): The labels.
					   It expects list of list of booleans.
  
	Returns:
		DataFrame: DataFrame that contains the data.
	'''
	data = []
 
	for i in range(len(Y_pred)):
		for pred in Y_pred[i]:
			data.append((Y_true[i][pred[0]], len(pred[2]), pred[3]))
	
	return DataFrame(data=data, columns=['Label', 'Length', 'Entropy'])

  
def plot(data: DataFrame, ptype: PlotType) -> Union[Axes, JointGrid]:
    '''
    Plots the given type of figure by using the given data.
	
	Args:
		data (DataFrame): The data.
						  It expects the data that 
        				  consists of label, length of generated tokens, 
              			  and line entorpy.
        ptype (PlotType): The plot type.
    
    Returns:
        Axes: The plotted Axes.
        JointGrid: The plotted JointGrid.
    '''
        
    if ptype == PlotType.LABEL_ENTROPY: 
        return sns.kdeplot(data=data, x='Entropy', hue='Label')
    elif ptype == PlotType.LABEL_LENGTH:
        return sns.kdeplot(data=data, x='Length', hue='Label')
    else:
        return sns.jointplot(data=data, x='Length', y='Entropy', hue='Label')
    
