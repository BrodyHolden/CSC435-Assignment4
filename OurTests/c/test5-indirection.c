#include <stdio.h>

int main() {
    int i = 5;

    int* p = &i;

    *p = 6;

    printf("%d\n", *p);
}
