import sentencepiece as spm
import sys
import os 

if len(sys.argv) != 3:
    print("Insufficient arguments")
    sys.exit()

os.chdir(sys.argv[1])
project_name = sys.argv[2]

dir_path = os.path.dirname(os.path.realpath(__file__))
spm.SentencePieceTrainer.Train('--pad_id=0 --unk_id=1 --bos_id=2 --eos_id=3 --user_defined_symbols=[SOS],[EOL],[EOS] --shuffle_input_sentence=true --unk_piece=[UNK] --pad_piece=[PAD] --input='+project_name+'_corpus.txt --model_prefix='+project_name+'_imdb --vocab_size=11723 --model_type=bpe --max_sentence_length=99999')

# sp = spm.SentencePieceProcessor()
# vocab_file = "imdb.model"
# sp.load(vocab_file)

# lines = [
#   "public void deleteSubscription(String authtoken, String key) throws Exception {[EOL]",
#   "if (authtoken == null) {[EOL]",
#   "authtoken = clerk.getAuthToken();[EOL]",
#   "}[EOL]",
#   "if (key == null) {[EOS]"
# ]
# for line in lines:
#   print(line)
#   print(sp.encode_as_pieces(line))
#   print(sp.encode_as_ids(line))
#   print()
