The column recognizer code is at a pre-release level. Essential elements still
missing include:

- unit tests
- good error handling
- a documented way of training models


To run the code, run the ColumnRecognizerPrototype class with 
column-recognizers\sample-data as the working directory.

To compute inverse column frequencies for a corpus of tables, run the 
InverseColumnFrequency class with 
- the list of CSV tables and column separators as arguments
- column-recognizers\sample-data as the working directory

See also the JavaDoc comments in the InverseColumnFrequency class.