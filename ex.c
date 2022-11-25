#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>


#define MAX_ARGS 10

int main(int argc, char *argv[]) {
	char *newargv[MAX_ARGS];
	char *newenv[] = {NULL};

	for(int i = 0; i < argc; i++) printf("%s\n", argv[i]);

	for(int i = 1; i < argc; i++) newargv[i - 1] = argv[i];
	newargv[argc - 1] = NULL;

	execve(argv[1], newargv, newenv);

}