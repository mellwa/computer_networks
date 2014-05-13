JAVA = javac
exec1 = echo
exec2 = chmod
filename = router

all : 
	${JAVA} *.java
	${exec1} "#!/bin/bash\njava router $$"1 "$$"2 "$$"3 "$$"4 > ${filename}
	${exec2} 755 ${filename}

clean :
	rm -f *.class ${filename}
