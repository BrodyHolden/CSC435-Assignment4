package main

import "fmt"

type foo struct {
  i int
  ip *int
  f32 float32
  f64 float64

}

func main() {
    var f foo

    f.i = 0

    // TODO Default assignments.

    // Test default assignments
   // fmt.Printf("%d\n", f.a);
   // fmt.Printf("%d\n", f.b);
    // TODO other types
}
