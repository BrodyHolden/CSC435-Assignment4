#include <stdio.h>

struct foo {
    int i;
    int* ip;
    double f64;
};

int main() {

    struct foo f;

    f.i = 0;
    f.ip = &f.i;
    f.f64 = 0.0;

    printf("%d\n", f.i);
    printf("%d\n", *f.ip);
    printf("%f\n", f.f64);

}
