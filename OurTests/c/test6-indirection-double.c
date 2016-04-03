#include <stdio.h>

int main() {
    int i = 5;

    int* p = &i;

    int** p2 = &p;

    **p2 = 6;

    printf("%d\n", **p2);
}
