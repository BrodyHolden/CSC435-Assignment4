#include <stdio.h>

struct foo {
    int i;
    int* ip;
    float f32;
    double f64;
};

int main() {

    struct foo f;

    f.i = 0;
    f.ip = &f.i;
    f.f32 = 0.0;
    f.f64 = 0.0;

}
