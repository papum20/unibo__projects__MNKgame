#include <stdio.h>
#include <string.h>



char check(FILE *f) {
	char c;
	while((c = fgetc(f)) != EOF && c == ' ' && c != '\n');
	if(c != '\n') ungetc(c, f);
	return c;
}




int main(int argc, char *argv[]) {

	FILE *f = fopen(argv[1], "r");
/*
	char w[10];
	char c;
	printf("$");
	while((c = check(f)) != EOF && c != '\n') {
		fscanf(f, "%s", &w);
		printf("%s.", w);
	}
	printf("\n");
	printf("$");
	while((c = check(f)) != EOF && c != '\n') {
		fscanf(f, "%s", &w);
		printf("%s.", w);
	}
	printf("\n");
	printf("$");
	while((c = check(f)) != EOF && c != '\n') {
		fscanf(f, "%s", &w);
		printf("%s.", w);
	}
	printf("\n");
	printf("$");
	while((c = check(f)) != EOF && c != '\n') {
		fscanf(f, "%s", &w);
		printf("%s.", w);
	}
	printf("\n");
	printf("$");
	while((c = check(f)) != EOF && c != '\n') {
		fscanf(f, "%s", &w);
		printf("%s.", w);
	}
	printf("\n");
	printf("$");



return 0;*/
	char w[10];
	char c = fgetc(f);
	ungetc(c, f);
	printf("$");
	while(c != EOF && c != '\n') {
		fscanf(f, "%s", &w);
		fscanf(f, "%c", &c);

		printf("%s.", w);
	}
	printf("\n");
	c = fgetc(f);
	ungetc(c, f);
	printf("$");
	while(c != EOF && c != '\n') {
		fscanf(f, "%s", &w);
		fscanf(f, "%c", &c);

		printf("%s.", w);
	}
	printf("\n");
	c = fgetc(f);
	ungetc(c, f);
	printf("$");
	while(c != EOF && c != '\n') {
		fscanf(f, "%s", &w);
		fscanf(f, "%c", &c);

		printf("%s.", w);
	}
	printf("\n");
	c = fgetc(f);
	ungetc(c, f);
	printf("$");
	while(c != EOF && c != '\n') {
		fscanf(f, "%s", &w);
		fscanf(f, "%c", &c);

		printf("%s.", w);
	}
	printf("\n");
	c = fgetc(f);
	ungetc(c, f);
	printf("$");
	while(c != EOF && c != '\n') {
		fscanf(f, "%s", &w);
		fscanf(f, "%c", &c);

		printf("%s.", w);
	}
	printf("\n");
/*
	while(printf("%d",fscanf(f, "%c", &c)) &&
		printf("-%c_", c) && fscanf(f, "%s", &w) != EOF) {
		printf(".%s,\n",w);
		
	}*/

	fclose(f);

}