package main

import "fmt"

func main() {
    var d float64 = 123.456

    fmt.Printf("d: %f\n", d)

    d = +d
    fmt.Printf("+d: %f\n", d);

    d = -d
    fmt.Printf("-d: %f\n", d);
}
