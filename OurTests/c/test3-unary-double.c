#include <stdio.h>

int main() {
    double d = 123.456;

    printf("d: %f\n", d);

    d = +d;
    printf("+d: %f\n", d);

    d = -d;
    printf("-d: %f\n", d);
}
