#include <stdio.h>
#include <stdbool.h>

int main() {
    // Must use boolean variables instead of (1 == 1) otherwise
    // everything is optimized away into consts.

    bool b = (1 == 1);

    printf("(1 == 1): %d\n",  b);

    printf("!(1 == 1): %d\n",  !b);

    printf("!!(1 == 1): %d\n", !!b);

}
