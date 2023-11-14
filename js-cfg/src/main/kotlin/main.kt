import cfg.CfgBuilder
import cfg.render.Renderer

fun main() {
    val js = """
        function complex(p, t, r) {
            bar(p);
            let c = 1;

            if (c == 1) {
                let b = 2;
                bar(b);
            } else {
                let c = 10;
                tar(1);
            }

            if (t == 1) {
                // code
            } else {
                // code 2
            }

            for (let i = 0; i < 10; i++) {
                body(i);
            }

            let i = 0;
            do {
                body(i);
            } while (i < 10);

            while (true) {
                body1();
                body2();
                body3();
            }

            for (variable in object) {
                body();
            }
        }
        
        function forLoop() {
            for (let i = 0; i < 10; i++) {
                body(i);
            }
        }
        
        function forInLoop() {
            for (variable in collection) {
                body(i);
            }
        }
        
        function forOfLoop() {
            for (variable of collection) {
                body(i);
            }
        }

        function while_branch() {
            while (true) {
                if (bar) {
                    break;
                }
                foo();
            }
        }
        
        function multipleIfClauses() {
            if (a) {
                doWork1();
            } else if (b) {
                doWork2();
            }else if (c) {
                doWork3();
            }
        }
        
        function returnTest() {
            if (a) {
                doWork1();
                return
            }
            
            doWork2();
        }
        
        function continueTest() {
            for (let x = 0; x < 10; x++) {
                continue;
                
                bad();
            }
            
            doWork();
        }
        
        function multipleReturnsTest() {
            for (let x = 0; x < 10; x++) {
                if (x == 1) {
                    return;
                } else {
                    if (x == 2) {
                        return;   
                    } else {
                        foo();
                    }
                }
            }
            
            doWork();
        }
    """.trimIndent()

    val functionNode = AstProvider.parse(js)

    val graph = CfgBuilder().visitFile(functionNode)
    Renderer.render(graph)
}
