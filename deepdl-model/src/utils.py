import os

PROJECT_NAME = 'deepdl-model'

def getpd() -> str:
    path = __file__.split(os.sep)
    
    for element in reversed(path):
        if element == PROJECT_NAME:
            break
        else:
            path.pop()
    
    return os.sep.join(path)    


def topk(self, y_pred, y_true, k):
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

    if not y_true.contains(True):
      raise Exception('invalid labels')

    for i in range(len(y_pred)):
      if i == k:
        break
      
      if y_true[y_pred[i][0]]:
        return 1

    return 0


def RR(self, y_pred, y_true):
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
    
    if not y_true.contains(True):
      raise Exception('invalid labels')
  
    for i in range(len(y_pred)):
      if y_true[y_pred[i][0]]:
        return 1.0 / (i + 1)
    
    
def AP(self, y_pred, y_true):
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
    
    if not y_true.contains(True):
      raise Exception('invalid labels')
    
    total_precision = 0.0
    n_true = 0
    
    for i in range(len(y_pred)):
      if y_true[y_pred[i][0]]:
        n_true += 1
        total_precision += n_true / (i + 1)
    
    return total_precision / n_true