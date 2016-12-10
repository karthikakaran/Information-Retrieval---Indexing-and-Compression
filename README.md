# Information-Retrieval---Indexing-and-Compression
Indexing is performed followed by compression of posting list using Gamma code and Dictionary uising Delta code is done.

you build two versions of the index for a simple statistical retrieval system and also each version of the index shall be in uncompressed form and compressed form. 

Need to build: Index_Version1.uncompress and Index_Version2.uncompress for the Cranfield collection.
Version 1 of your index considers the terms in the dictionary to be lemmas of words,whereas version 2 of your index considers that the terms of the dictionary to bestems of the tokens you

 Before building the dictionaries of any of the two versions of index, it is recommended to remove the stop words.
 The terms in your 2nd version of the index should be stemmed with the Porter stemmer. 
 For every term in both versions of the indexed, store:
 - Document frequency (df): The number of documents that the term occurs in.
 - Term frequency (tf): The number of times that the term occurs in
each document, and
 - The list of documents containing the term. For each document from the posting lists you are also required to store the frequency of the most frequent term or stem in that document (max_tf), and the total number of word occurrences in the document (doclen). To be noted that the value of doclen includes the number of stop-words encountered in the respective documents.
Store the inverted lists in your own storage manager.

You also are required to build two compressed versions of your index:

Index_Version1.compressed and Index_Version2.compressed. To do so, you shall (a) compress the dictionaries of both versions of the index and compress the inverted lists before storing them.
In Index_Version1.compressed you shall use for dictionary compression a blocked compression with k=8 (in this version of the index, the dictionary contains terms) and for the posting file you shall be using gamma encoding for the gaps between documentids. Because the index also contains for each dictionary entry the df, and for each document the tf, the doclen and the max_tf, and all these four values are numbers, you should compress them using the gamma encoding as well.

In Index_Version2.compressed you shall use compression of the dictionary with frontcoding
and for the posting files you shall use delta codes to encode the gaps between
document-ids. Because the index also contains for each dictionary entry the df, and for
each document the tf, the doclen and the max_tf, and all these four values are
numbers, you should compress them using the delta encoding as well.

Delta codes are similar to the gamma codes: they represent a gap by a pair: (length, offset). First the number is represented in binary code. The length of the binary representation is encoded in gamma code, prior to removing the leading 1-bit. After
generating the code of the length only, the leading 1-bit is removed and represented in gamma code.
