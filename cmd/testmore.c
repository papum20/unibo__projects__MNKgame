#include <dirent.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <unistd.h>


#define JAVA_DFLT		"myjava"
#define COMMAND_DFLT	"debug/testmore/in/testcmd.txt"
#define PLAYERS_DFLT	"debug/testmore/in/testlist.txt"
#define OUT_NAME		"test"
#define OUT_PATH		"debug/testmore/out/"
#define OUT_EXTENSION	".txt"

#define MSG_USAGE "\
Executes [java-executable] (which must execute java)\n\
with fixed options/parameters stored in [command]\n\
and variable options/parameters stored in [players],\n\
then each execution, corresponding to each line of players (txt) file, stores the result in [output-filename]-<number>.txt.\n\
\n\
Usage: %s [-c command] [-j java-executable] [-o output-filename] [-p players] [-v]\n\
\t-v: verbose (also print moves)\n\
\n\
Defaults:\n\
\tjava-executable = ./myjava\n\
\tcommand = debug/testmore/in/testcmd.txt\n\
\tplayers = debug/testmore/in/testlist.txt\n\
\toutput-filename = debug/testmore/out/test-<number>.txt\n\
\n\
Regarding java executable:\n\
\tfor Linux: just put a (symbolic) link to java executable\n\
\tfor Windows: create a myjava.bat, containing the following line:\n\
\t\tstart /b cmd /c 'C:\\path\\to\\java\\Oracle\\Java\\javapath\\java.exe' %%*\n\
\n\
Examples:\n\
\ttestcmd.txt:\n\
\t\t-cp out -Xmx3g mnkgame.MNKPlayerTester 7 7 4 -r 3 -t 3\n\
\ttestlist.txt:\n\
\t\tplayer.pnsearch.array.obj.PnSearchADeleteD mnkgame.QuasiRandomPlayer\n\
\t\tplayer.pnsearch.array.obj.PnSearchAUpdateD mnkgame.QuasiRandomPlayer\n\
\n\
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
char *toString(int n, char *s) {
	if(n > 0 || s == NULL) {
		if(s == NULL) s = malloc(sizeof(char) * ARG_LEN);
			//s[0] = '\0';
		toString(n / 10, s);
		s[strlen(s)] = (char)(n % 10 + 48);
		s[strlen(s)] = '\0';
	}
	return s;
}

// read f until end of line/file, store the words in strings, starting from index start
int readLine_storeArray(FILE *f, char *strings[], int start) {
	int read = 0;
	char c, w[ARG_LEN];
	while((c = nextWord(f, NULL, NULL)) != EOF && c != '\n') {
		fscanf(f, "%s", w);
		int i = start + read++;
		strings[i] = stringCopy(w);
	}
	return read;
}

int stringStartsWith(const char *s, const char *start) {
	int i = 0;
	while(i < strlen(s) && i < strlen(start) && s[i] == start[i]) i++;
	return (i == strlen(start));
}

int findFilenameStart(const char *filename) {
	char *dirname = OUT_PATH;
	struct dirent *d;
	int found;

	DIR *dir = opendir(dirname);
	while((d = readdir(dir)) != NULL && !(found = stringStartsWith(d->d_name, filename)) );
	closedir(dir);

	return found;
}





int main(int argc, char *argv[]) {

	//parameters for execve
	char *newargv[ARG_MAX];
	char *newenv[] = {NULL};


	//retrieve command and parameters
	char *java_exe = NULL, *command_txt = NULL, *players_txt = NULL, *out_txt = NULL;	//files to read
	char *out_path;
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
	if(out_txt == NULL) out_txt = stringCopy(OUT_NAME);
	//make such that the out name is not used
	int num = 0;
	char *out_t = malloc(sizeof(char) * ARG_LEN);
	do {
		strcpy(out_t, out_txt);
		char *num_str = toString(num++, NULL);
		strcat(out_t, num_str);
		strcat(out_t, "-");
		free(num_str);
	} while(findFilenameStart(out_t));
	free(out_txt);
	out_txt = out_t;
	
	out_path = stringCopy(OUT_PATH);
	strcat(out_path, out_txt);


	//read parameters for execve from command and players
	FILE *f;					//file stream
	size_t bufsize = 0;
	int command_len, player_len;
	int test_number;
	char c, w[ARG_LEN];			//for implementation (while loops)

	//retrieve java executable
	newargv[0] = stringCopy(java_exe);
	//retrieve command paramters
	f = fopen(command_txt, "r");
	command_len = readLine_storeArray(f, newargv, 1);
	fclose(f);

	//DEBUG
	printf("%s\n%s\n%s\n%s\n\n", java_exe, command_txt, players_txt, out_txt);
	for(int i = 0; i < 1 + command_len; i++) printf("%d\t- %s\n", i, newargv[i]);

	//execute command with each line of players
	f = fopen(players_txt, "r");
	c = '.';				//any char != EOF
	test_number = 0;
	while(c != EOF)
	{
		//retrieve players, and put them in newargv
		player_len = 0;
		while((c = nextWord(f, NULL, NULL)) != EOF && c != '\n') {
			fscanf(f, "%s", w);
			int i = 1 + command_len + player_len++;
			newargv[i] = stringCopy(w);
		}
		
		newargv[1 + command_len + player_len] = NULL;

		//DEBUG
		for(int i = 0; i < 1 + command_len + player_len; i++) printf("%d\t- %s\n", i, newargv[i]);
	


		//execute
		if(fork() == 0) {
			//child processs
			char *out = malloc(sizeof(char) * ARG_LEN);
			strcpy(out, out_path);
			char number[2] = {test_number+48, '\0'};
			strcat(out, number);
			strcat(out, OUT_EXTENSION);
			//open output file
			int out_fd = open(out, O_RDWR | O_CREAT, S_IRUSR | S_IWUSR);
			dup2(out_fd, 1);	//make stdout go to file
			dup2(out_fd, 2);	//make stderr go to file
			close(out_fd);
			//exec
			execve(java_exe, newargv, newenv);
			free(out);
			break;
		} else {
			//parent process
			wait(NULL);
		}
		test_number++;
	}
	fclose(f);


	// CLEAN MEMORY
	free(java_exe);
	free(command_txt);
	free(players_txt);
	free(out_txt);
	free(out_path);
	for(int i = 0; newargv[i] != NULL; i++) free(newargv[i]);

}