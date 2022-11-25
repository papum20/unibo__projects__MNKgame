#include <stdio.h>
#include <sys/wait.h>
#include <unistd.h>

int main() {
	if(fork() == 0) {

		for(int i = 0; i < 20; i++) {
			printf("\t\tchild: %d\n", i);
		}

	} else {

		while(wait(NULL) > 0) {}

		for(int i = 0; i < 20; i++) {
			printf("parent: %d\n", i);
		}
		
	}

}