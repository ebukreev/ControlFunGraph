fun foo2(n: Int): Int {
    var a = 0
    var b = 1
    var c: Int = 0
    var i = 4
    do {
        c = a + b
        a = b
        var j = 2
        do {
            a = c
        } while (j < 5)
        b = c
    } while (i < n)
    return c
}