fun foo(n: Int): Int {
    var a = 0
    var b = 1
    var c: Int = 0
    if (n < 2) c = n else  {
        c = 5
        b = 6
    }
    for (i in 1..n) {
        c = a + b
        a = b
        for (j in 5..6) {
            a = c
            continue
        }
        break
        b = c
    }
    return c
}