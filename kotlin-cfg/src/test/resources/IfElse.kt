fun foo() {
    var x = 6
    var z = 9
    if (x < 4) {
        z = 5
    } else {
        x = 4
    }

    if (x > 4) z = 6

    if (z > 8) return else if (x < 5) z = 8

    return
}