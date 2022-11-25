#include <stdio.h>
#include <string.h>
#include <stdlib.h>

void addString(char *strings[], int ind, const char *s) {
	char tmp[10];
	strcpy(tmp, s);
	strings[ind] = tmp;
}

int main() {
	char *c[2];
	char d[] = "ciao";

	strcpy(c[0], d);
	printf("%s, %s\n", c[0], d);
	strcpy(d, "oaic");
	printf("%s, %s\n", c[0], d);
}