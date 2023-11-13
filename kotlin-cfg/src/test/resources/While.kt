fun foo1(n: Int): Int {
    var a = 0
    var b = 1
    var c: Int = 0
    var i = 4
    while (i < n) {
        c = a + b
        a = b
        var j = 2
        while (j < 5) {
            a = c
            continue
        }
        break
        b = c
    }
    return c
}