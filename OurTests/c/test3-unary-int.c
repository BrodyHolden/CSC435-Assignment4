#include <stdio.h>

int main() {
    int a = 5;

    printf("a: %d\n", a);

    a = +a;
    printf("+a: %d\n", a);

    a = -a;
    printf("-a: %d\n", a);
}
