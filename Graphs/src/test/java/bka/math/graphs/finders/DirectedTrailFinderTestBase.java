/*
** © Bart Kampers
*/

package bka.math.graphs.finders;

import bka.math.graphs.*;
import java.util.*;

public abstract class DirectedTrailFinderTestBase extends TrailFinderTestBase {

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
                List.of(AB),
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
                        B, null,
                        Collections.emptyList(),
                        Collections.emptyList()
                    )
                )
            ),
            new TestCase( // fork of two edges
                List.of(AB, AC),
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
                    ),
                    new ExpectedTrails(
                        B, null,
                        Collections.emptyList(),
                        Collections.emptyList()
                    ),
                    new ExpectedTrails(
                        C, null,
                        Collections.emptyList(),
                        Collections.emptyList()
                    )
                )
            ),
            new TestCase( // trail of two edges
                List.of(AB, BC),
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
                List.of(AA),
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
                List.of(AB, BA),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AB)),
                        List.of(trail(AB), trail(AB, BA))
                    ),
                    new ExpectedTrails(
                        A, A,
                        List.of(trail(AB, BA)),
                        List.of(trail(AB, BA))
                    ),
                    new ExpectedTrails(
                        A, B,
                        List.of(trail(AB)),
                        List.of(trail(AB))
                    ),
                    new ExpectedTrails(
                        B, null,
                        List.of(trail(BA)),
                        List.of(trail(BA), trail(BA, AB))
                    ),
                    new ExpectedTrails(
                        B, A,
                        List.of(trail(BA)),
                        List.of(trail(BA))
                    ),
                    new ExpectedTrails(
                        B, B,
                        List.of(trail(BA, AB)),
                        List.of(trail(BA, AB))
                    )
                )
            ),
            new TestCase( // cycle of three edges
                List.of(AB, BC, CA),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AB), trail(AB, BC)),
                        List.of(trail(AB), trail(AB, BC), trail(AB, BC, CA))
                    ),
                    new ExpectedTrails(
                        A, A,
                        List.of(trail(AB, BC, CA)),
                        List.of(trail(AB, BC, CA))
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
                    ),
                    new ExpectedTrails(
                        B, null,
                        List.of(trail(BC), trail(BC, CA)),
                        List.of(trail(BC), trail(BC, CA), trail(BC, CA, AB))
                    ),
                    new ExpectedTrails(
                        B, C,
                        List.of(trail(BC)),
                        List.of(trail(BC))
                    ),
                    new ExpectedTrails(
                        B, A,
                        List.of(trail(BC, CA)),
                        List.of(trail(BC, CA))
                    ),
                    new ExpectedTrails(
                        B, B,
                        List.of(trail(BC, CA, AB)),
                        List.of(trail(BC, CA, AB))
                    ),
                    new ExpectedTrails(
                        C, null,
                        List.of(trail(CA), trail(CA, AB)),
                        List.of(trail(CA), trail(CA, AB), trail(CA, AB, BC))
                    ),
                    new ExpectedTrails(
                        C, A,
                        List.of(trail(CA)),
                        List.of(trail(CA))
                    ),
                    new ExpectedTrails(
                        C, B,
                        List.of(trail(CA, AB)),
                        List.of(trail(CA, AB))
                    ),
                    new ExpectedTrails(
                        C, C,
                        List.of(trail(CA, AB, BC)),
                        List.of(trail(CA, AB, BC))
                    )
                )
            ),
            new TestCase( // loop with fork
                List.of(AA, AB),
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
                        Collections.emptyList(),
                        Collections.emptyList())
                )
            ),
            new TestCase( // cycle with fork
                List.of(AB, BC, CA, AD),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AB), trail(AB, BC), trail(AD)),
                        List.of(trail(AB), trail(AB, BC), trail(AB, BC, CA), trail(AB, BC, CA, AD), trail(AD))
                    ),
                    new ExpectedTrails(
                        A, A,
                        List.of(trail(AB, BC, CA)),
                        List.of(trail(AB, BC, CA))
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
                    ),
                    new ExpectedTrails(
                        A, D,
                        List.of(trail(AD)),
                        List.of(trail(AB, BC, CA, AD), trail(AD))
                    )
                )
            ),
            new TestCase( // circuit
                List.of(AB, BA, AC, CA),
                List.of(
                    new ExpectedTrails(
                        A, null,
                        List.of(trail(AB), trail(AC)),
                        List.of(trail(AB), trail(AB, BA), trail(AB, BA, AC), trail(AB, BA, AC, CA), trail(AC), trail(AC, CA), trail(AC, CA, AB), trail(AC, CA, AB, BA))
                    ),
                    new ExpectedTrails(
                        A, A,
                        List.of(trail(AB, BA), trail(AC, CA)),
                        List.of(trail(AB, BA), trail(AB, BA, AC, CA), trail(AC, CA), trail(AC, CA, AB, BA))
                    ),
                    new ExpectedTrails(
                        A, B,
                        List.of(trail(AB)),
                        List.of(trail(AB), trail(AC, CA, AB))
                    ),
                    new ExpectedTrails(
                        A, C,
                        List.of(trail(AC)),
                        List.of(trail(AC), trail(AB, BA, AC))
                    )
                )
            )
        );
    }

    private static final Object A = "a";
    private static final Object B = "b";
    private static final Object C = "c";
    private static final Object D = "d";

    private static final DirectedEdge<Object> AA = new DirectedEdge<>(A, A);
    private static final DirectedEdge<Object> AB = new DirectedEdge<>(A, B);
    private static final DirectedEdge<Object> AC = new DirectedEdge<>(A, C);
    private static final DirectedEdge<Object> AD = new DirectedEdge<>(A, D);
    private static final DirectedEdge<Object> BA = new DirectedEdge<>(B, A);
    private static final DirectedEdge<Object> BC = new DirectedEdge<>(B, C);
    private static final DirectedEdge<Object> CA = new DirectedEdge<>(C, A);

}
