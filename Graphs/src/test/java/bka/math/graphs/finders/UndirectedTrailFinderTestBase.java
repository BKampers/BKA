/*
** © Bart Kampers
*/

package bka.math.graphs.finders;

import bka.math.graphs.*;
import java.util.*;

public abstract class UndirectedTrailFinderTestBase extends TrailFinderTestBase {

    @Override
    protected List<TestCase> getTestCases() {
        return List.of(
            new TestCase( // empty graph
                Collections.emptyList(),
                List.of(
                    new ExpectedTrails(
                        new Object(), null,
                        Collections.emptyList(),
                        Collections.emptyList()
                    ),
                    new ExpectedTrails(
                        new Object(), new Object(),
                        Collections.emptyList(),
                        Collections.emptyList()
                    )
                )
            ),
            new TestCase( // one edge
                graph(AB),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AB)),
                        List.of(trail(AB))
                    ),
                    new ExpectedTrails(
                        A, A,
                        Collections.emptyList(),
                        Collections.emptyList()
                    ),
                    new ExpectedTrails(
                        A, B,
                        List.of(trail(AB)),
                        List.of(trail(AB))
                    ),
                    new ExpectedTrails(
                        B, A,
                        List.of(trail(AB)),
                        List.of(trail(AB))
                    ),
                    new ExpectedTrails(
                        B, B,
                        Collections.emptyList(),
                        Collections.emptyList()
                    )
                )
            ),
            new TestCase( // fork of two edges
                graph(AB, AC),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AB), trail(AC)),
                        List.of(trail(AB), trail(AC))
                    ),
                    new ExpectedTrails(
                        A, B,
                        List.of(trail(AB)),
                        List.of(trail(AB))
                    ),
                    new ExpectedTrails(
                        A, C,
                        List.of(trail(AC)),
                        List.of(trail(AC))
                    )
                )
            ),
            new TestCase( // trail of two edges
                graph(AB, BC),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AB), trail(AB, BC)),
                        List.of(trail(AB), trail(AB, BC))
                    ),
                    new ExpectedTrails(
                        A, B,
                        List.of(trail(AB)),
                        List.of(trail(AB))
                    ),
                    new ExpectedTrails(
                        A, C,
                        List.of(trail(AB, BC)),
                        List.of(trail(AB, BC))
                    )
                )
            ),
            new TestCase( // loop
                graph(AA),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AA)),
                        List.of(trail(AA))
                    ),
                    new ExpectedTrails(
                        A, A,
                        List.of(trail(AA)),
                        List.of(trail(AA))
                    )
                )
            ),
            new TestCase( // cycle of two edges
                graph(AB, BA),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AB), trail(BA)),
                        List.of(trail(AB), trail(AB, BA), trail(BA), trail(BA, AB))
                    ),
                    new ExpectedTrails(
                        A, A,
                        List.of(trail(AB, BA), trail(BA, AB)),
                        List.of(trail(AB, BA), trail(BA, AB))
                    ),
                    new ExpectedTrails(
                        A, B,
                        List.of(trail(AB), trail(BA)),
                        List.of(trail(AB), trail(BA))
                    )
                )
            ),
            new TestCase( // cycle of three edges
                graph(AB, BC, CA),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AB), trail(CA), trail(AB, BC), trail(CA, BC)),
                        List.of(trail(AB), trail(CA), trail(AB, BC), trail(CA, BC), trail(AB, BC, CA), trail(CA, BC, AB))
                    ),
                    new ExpectedTrails(
                        A, A,
                        List.of(trail(AB, BC, CA), trail(CA, BC, AB)),
                        List.of(trail(AB, BC, CA), trail(CA, BC, AB))
                    ),
                    new ExpectedTrails(
                        A, B,
                        List.of(trail(AB), trail(CA, BC)),
                        List.of(trail(AB), trail(CA, BC))
                    ),
                    new ExpectedTrails(
                        A, C,
                        List.of(trail(CA), trail(AB, BC)),
                        List.of(trail(CA), trail(AB, BC))
                    )
                )
            ),
            new TestCase( // loop with fork
                graph(AA, AB),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AA), trail(AB)),
                        List.of(trail(AA), trail(AA, AB), trail(AB))
                    ),
                    new ExpectedTrails(
                        A, A,
                        List.of(trail(AA)),
                        List.of(trail(AA))
                    ),
                    new ExpectedTrails(
                        A, B,
                        List.of(trail(AB)),
                        List.of(trail(AA, AB), trail(AB))
                    ),
                    new ExpectedTrails(
                        B, null,
                        List.of(trail(AB)),
                        List.of(trail(AB), trail(AB, AA))
                    ),
                    new ExpectedTrails(
                        B, A,
                        List.of(trail(AB)),
                        List.of(trail(AB), trail(AB, AA))
                    )
                )
            ),
            new TestCase( // cycle with fork
                graph(AB, BC, CA, AD),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AB), trail(CA), trail(CA, BC), trail(AB, BC), trail(AD)),
                        List.of(trail(AB), trail(AB, BC), trail(AB, BC, CA), trail(AB, BC, CA, AD), trail(CA), trail(CA, BC), trail(CA, BC, AB), trail(CA, BC, AB, AD), trail(AD))
                    ),
                    new ExpectedTrails(
                        A, A,
                        List.of(trail(AB, BC, CA), trail(CA, BC, AB)),
                        List.of(trail(AB, BC, CA), trail(CA, BC, AB))
                    ),
                    new ExpectedTrails(
                        A, B,
                        List.of(trail(AB), trail(CA, BC)),
                        List.of(trail(AB), trail(CA, BC))
                    ),
                    new ExpectedTrails(
                        A, C,
                        List.of(trail(CA), trail(AB, BC)),
                        List.of(trail(CA), trail(AB, BC))
                    ),
                    new ExpectedTrails(
                        A, D,
                        List.of(trail(AD)),
                        List.of(List.of(CA, BC, AB, AD), trail(AB, BC, CA, AD), trail(AD))
                    )
                )
            ),
            new TestCase( // circuit
                graph(AB, BA, AC, CA),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AB), trail(BA), trail(AC), trail(CA)),
                        List.of(trail(AB), trail(AB, BA), trail(AB, BA, AC), trail(AB, BA, AC, CA), trail(AB, BA, CA), trail(AB, BA, CA, AC), trail(BA), trail(BA, AB), trail(BA, AB, AC), trail(BA, AB, AC, CA), trail(BA, AB, CA), trail(BA, AB, CA, AC), trail(AC), trail(AC, CA), trail(AC, CA, AB), trail(AC, CA, AB, BA), trail(AC, CA, BA), trail(AC, CA, BA, AB), trail(CA), trail(CA, AC), trail(CA, AC, AB), trail(CA, AC, AB, BA), trail(CA, AC, BA), trail(CA, AC, BA, AB))
                    ),
                    new ExpectedTrails(
                        A, A,
                        List.of(trail(AB, BA), trail(BA, AB), trail(AC, CA), trail(CA, AC)),
                        List.of(trail(AB, BA), trail(AB, BA, AC, CA), trail(AB, BA, CA, AC), trail(BA, AB), trail(BA, AB, AC, CA), trail(BA, AB, CA, AC), trail(AC, CA), trail(AC, CA, AB, BA), trail(AC, CA, BA, AB), trail(CA, AC), trail(CA, AC, AB, BA), trail(CA, AC, BA, AB))
                    ),
                    new ExpectedTrails(
                        A, B,
                        List.of(trail(AB), trail(BA)),
                        List.of(trail(AB), trail(BA), trail(AC, CA, AB), trail(AC, CA, BA), trail(CA, AC, AB), trail(CA, AC, BA))
                    ),
                    new ExpectedTrails(
                        A, C,
                        List.of(trail(AC), trail(CA)),
                        List.of(trail(AB, BA, AC), trail(AB, BA, CA), trail(BA, AB, AC), trail(BA, AB, CA), trail(AC), trail(CA))
                    )
                )
            )
        );
    }

    private static final Object A = 'a';
    private static final Object B = 'b';
    private static final Object C = 'c';
    private static final Object D = 'd';

    private static final UndirectedEdge<Object> AA = new UndirectedEdge<>(A, A);
    private static final UndirectedEdge<Object> AB = new UndirectedEdge<>(A, B);
    private static final UndirectedEdge<Object> AC = new UndirectedEdge<>(A, C);
    private static final UndirectedEdge<Object> AD = new UndirectedEdge<>(A, D);
    private static final UndirectedEdge<Object> BA = new UndirectedEdge<>(B, A);
    private static final UndirectedEdge<Object> BC = new UndirectedEdge<>(B, C);
    private static final UndirectedEdge<Object> CA = new UndirectedEdge<>(C, A);

}
