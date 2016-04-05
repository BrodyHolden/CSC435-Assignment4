package main

import "fmt"

type foo struct {
  i int
  ip *int
  f64 float64

}

func main() {
    var f foo

    f.i = 0
    f.ip = &f.i
    f.f64 = 0.0

    fmt.Printf("%d\n", f.i);
    fmt.Printf("%d\n", *f.ip);
    fmt.Printf("%f\n", f.f64);

}
