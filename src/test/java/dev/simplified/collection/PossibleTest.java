package dev.simplified.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Coverage of the one thing {@link Possible} exists for: that the two value-less states are told
 * apart, and stay told apart through the operations a caller chains onto them.
 */
@DisplayName("Possible tells empty and absent apart")
class PossibleTest {

    @Test
    @DisplayName("empty and absent are not equal, and hash apart")
    void theTwoStatesAreDistinct() {
        assertThat(Possible.empty(), is(not(Possible.absent())));
        assertThat(Possible.absent(), is(not(Possible.empty())));
        assertThat(Possible.empty().hashCode() == Possible.absent().hashCode(), is(false));
    }

    @Test
    @DisplayName("each state is equal to itself")
    void eachStateIsReflexive() {
        assertThat(Possible.empty(), is(Possible.empty()));
        assertThat(Possible.absent(), is(Possible.absent()));
        assertThat(Possible.of("x"), is(Possible.of("x")));
        assertThat(Possible.of("x"), is(not(Possible.of("y"))));
    }

    @Test
    @DisplayName("the predicates are exclusive, and isEmpty is not the negation of isPresent")
    void thePredicatesAreExclusive() {
        assertThat(Possible.empty().isEmpty(), is(true));
        assertThat(Possible.empty().isAbsent(), is(false));
        assertThat(Possible.absent().isAbsent(), is(true));
        // The documented trap: an absent Possible answers false here where an Optional would answer true.
        assertThat(Possible.absent().isEmpty(), is(false));
        assertThat(Possible.absent().isMissing(), is(true));
        assertThat(Possible.empty().isMissing(), is(true));
        assertThat(Possible.of("x").isMissing(), is(false));
    }

    @Test
    @DisplayName("a value-less state survives map and flatMap")
    void theReasonSurvivesTransformation() {
        assertThat(Possible.absent().map(Object::toString).isAbsent(), is(true));
        assertThat(Possible.empty().map(Object::toString).isEmpty(), is(true));
        assertThat(Possible.absent().flatMap(Possible::of).isAbsent(), is(true));
        assertThat(Possible.empty().flatMap(Possible::of).isEmpty(), is(true));
    }

    @Test
    @DisplayName("orAbsent answers for absence alone and leaves an empty one empty")
    void orAbsentIsSelective() {
        assertThat(Possible.absent().orAbsent(() -> Possible.of("sub")).orElse(null), is("sub"));
        assertThat(Possible.empty().orAbsent(() -> Possible.of("sub")).isEmpty(), is(true));
        assertThat(Possible.of("kept").orAbsent(() -> Possible.of("sub")).orElse(null), is("kept"));
    }

    @Test
    @DisplayName("a rejected filter yields empty rather than absent, because the value was there to test")
    void filterYieldsEmpty() {
        assertThat(Possible.of("x").filter(v -> false).isEmpty(), is(true));
        assertThat(Possible.of("x").filter(v -> false).isAbsent(), is(false));
        assertThat(Possible.absent().filter(v -> true).isAbsent(), is(true));
    }

    @Test
    @DisplayName("converting to Optional is lossy in the one direction Optional cannot carry")
    void optionalInteropIsLossy() {
        assertThat(Possible.empty().toOptional(), is(Optional.empty()));
        assertThat(Possible.absent().toOptional(), is(Optional.empty()));
        assertThat(Possible.ofOptional(Optional.empty()).isEmpty(), is(true));
        assertThat(Possible.ofOptional(Optional.of("x")).orElse(null), is("x"));
    }

}
