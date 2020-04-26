#!/bin/bash
# Ensure you have wget
# Run me like this ./run.sh <test-case-number>
# Example ./run.sh 1 

folder=TestCases/TestCase$1
code="Code.java"
testcase="test.txt"

wget https://mirrors.gigenet.com/apache//commons/bcel/binaries/bcel-6.4.1-bin.tar.gz
tar -zxvf  bcel-6.4.1-bin.tar.gz >/dev/null 2>&1;

javac -cp bcel-6.4.1/bcel-6.4.1.jar:. proj/CFG.java proj/GraphGenerator.java proj/PrimePath.java 

cd $folder;
javac -cp . $code;

cd -
code_class=`echo $code | sed 's/.java//'`

java -cp bcel-6.4.1/bcel-6.4.1.jar:$folder:. proj.CFG $code_class > $folder/graph.txt 

if stat "$folder/$testcase" > /dev/null 2>&1 ; then
	java -cp . proj.PrimePath $folder/graph.txt $folder/$testcase
else
	java -cp . proj.PrimePath $folder/graph.txt
fi  
