#include <stdio.h>
#include <stdlib.h>
#include <string.h>

extern int _global_init();

int main() {
    return _global_init();
}

void __print(char* str) {
	printf("%s", str + 8);
}

void __println(char* str) {
	puts(str + 8);
}

char* __getString() {
	static char __buffer[1024 * 1024];	//	1MB buffer
	scanf("%s", __buffer);
	int len = strlen(__buffer);
	char* res = (char*) malloc(len + 8);
	*((long*)res) = len;
	strcpy(res + 8, __buffer);
	return res;
}

long __getInt() {
	long res;
	scanf("%ld", &res);
	return res;
}

char* __toString(long num) {
    char *res = (char*) malloc(8 + 24);
    *((long*) res) = sprintf(res + 8, "%ld", num);
    return res;
}

long __string_length(char* str) {
    return *((long*) str);
}

char* __string_substring(char* str, long l, long r) {
	int len = r - l + 1;
	char* res = (char*) malloc(9 + len);
	*((long*) res) = len;
	str += 8 + l;
	for(int i = 0; i < len; i++)
		res[8 + i] = str[i];
	res[8 + len] = 0;
	return res;
}

long __string_parseInt(char* str) {
    str += 8;
    int neg = 0;
    if(*str == '-') {
        neg  = 1;
        str++;
    }
    int num = 0;
    while (*str >= '0' && *str <= '9') {
        num = num * 10 + (*str - '0');
        str++;
    }
    return neg ? -num : num;
}

long __string_ord(char* str, long idx) {
    return str[idx + 8];
}

char* __string_concat(char* str1, char* str2) {
    long len1 = *((long*) str1);
    long len2 = *((long*) str2);
    char *res = (char*) malloc(9 + len1 + len2);
    *((long*) res) = len1 + len2;
    int now = 8;
    for(int i = 0; i < len1; ++i)
        res[now++] = str1[i + 8];
    for(int i = 0; i < len2; ++i)
        res[now++] = str2[i + 8];
    res[now++] = 0;
    return res;
}

long __string_compare(char* str1, char* str2) {
    return strcmp(str1 + 8, str2 + 8);
}