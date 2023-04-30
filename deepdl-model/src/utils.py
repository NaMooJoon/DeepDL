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