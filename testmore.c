#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>


#define JAVA_DFLT		"myjava"
#define COMMAND_DFLT	"debug/testmore/in/testcmd.txt"
#define PLAYERS_DFLT	"debug/testmore/in/testlist.txt"
#define OUT_NAME		"test_out"
#define OUT_PATH		"debug/testmore/out/"
#define OUT_EXTENSION	".txt"

#define MSG_USAGE "\
					Usage: %s [-c command] [-j java-executable] [-o output-filename] [-p players] [-v]\n\
					\t-v: verbose (also print moves)\
					"

#define ARG_MAX 20
#define ARG_LEN 50

#define SKIP_DFLT " \r"
#define STOP_DFLT "\n"



int findChar(char c, char *str) {
	for(int i = 0; i < strlen(str); i++)
		if(str[i] == c) return 1;
	return 0;
}
char nextWord(FILE *f, char *skip, char *stop) {
	if(skip == NULL) skip = SKIP_DFLT;
	if(stop == NULL) stop = STOP_DFLT;
	char c;
	while((c = getc(f)) != EOF && findChar(c, skip));
	if(!findChar(c, stop)) ungetc(c, f);
	return c;
}

char *stringCopy(char *s) {
	//char *t = malloc(sizeof(char) * strlen(s));
	char *t = malloc(sizeof(char) * ARG_LEN);
	strcpy(t, s);
	return t;
}




int main(int argc, char *argv[]) {

	//parameters for execve
	char *newargv[ARG_MAX];
	char *newenv[] = {NULL};


	//retrieve command and parameters
	char *java_exe = NULL, *command_txt = NULL, *players_txt = NULL, *out_txt = NULL;	//files to read
	int verbose = 0;
	int opt;

	while((opt = getopt(argc, argv, "c:j:o:p:v")) != -1) {
		switch(opt) {
			case 'c':
				command_txt = stringCopy(optarg);
				break;
			case 'j':
				java_exe = stringCopy(optarg);
				break;
			case 'o':
				out_txt = stringCopy(optarg);
				break;
			case 'p':
				players_txt = stringCopy(optarg);
				break;
			case 'v':
				verbose = 1;
				break;
			default:
				fprintf(stderr, MSG_USAGE, argv[0]);
				exit(EXIT_FAILURE);
		}
	}

	//default parameters
	if(java_exe == NULL)	java_exe	= stringCopy(JAVA_DFLT);
	if(command_txt == NULL) command_txt	= stringCopy(COMMAND_DFLT);
	if(players_txt == NULL) players_txt = stringCopy(PLAYERS_DFLT);
	if(out_txt == NULL) {
		out_txt = stringCopy(OUT_NAME);
	}
	char *out_path = stringCopy(OUT_PATH);
	strcat(out_path, out_txt);
	free(out_txt);
	out_txt = out_path;
	strcat(out_txt, OUT_EXTENSION);

	//DEBUG
	printf("%s\n%s\n%s\n%s\n\n", java_exe, command_txt, players_txt, out_txt);


	//read parameters for execve from command and players
	FILE *f;					//file stream
	size_t bufsize = 0;

	int command_len, player_len;
	char c, w[ARG_LEN];			//for implementation (while loops)

	//retrieve java executable
	newargv[0] = stringCopy(java_exe);

	//DEBUG
	printf("%s\n\n", newargv[0]);

	//retrieve command paramters
	command_len = 0;
	f = fopen(command_txt, "r");
	while(fscanf(f, "%s", w) != EOF) {
		int newargv_i = 1 + command_len++;
		newargv[newargv_i] = stringCopy(w);
	}
	fclose(f);

	//DEBUG
	for(int i = 0; i < 1 + command_len; i++) printf("%d - %s\n", i, newargv[i]);

	//open output file
	int out_fd = open(out_txt, O_RDWR | O_CREAT, S_IRUSR | S_IWUSR);
	dup2(out_fd, 1);	//make stdout go to file
	dup2(out_fd, 2);	//make stderr go to file
	close(out_fd);

	//execute command with each line of players
	f = fopen(players_txt, "r");
	c = '.';
	while(c != EOF) {
		//retrieve players, and put them in newargv
		char arg[ARG_LEN];
		player_len = 0;
		while((c = nextWord(f, NULL, NULL)) != EOF && c != '\n') {
			printf("c:%c-%d-%d.\n",c,c,c=='\n');
			fscanf(f, "%s", arg);
			int newargv_i = 1 + command_len + player_len++;
			newargv[newargv_i] = stringCopy(arg);
		}
		newargv[1 + command_len + player_len] = NULL;

		//DEBUG
		for(int i = 0; i < 1 + command_len + player_len; i++) printf("%d - %s\n", i, newargv[i]);
	
		//execute
		execve(java_exe, newargv, newenv);
	}
	fclose(f);




}