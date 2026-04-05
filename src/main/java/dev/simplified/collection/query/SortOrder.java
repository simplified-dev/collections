package dev.simplified.collection.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the direction of a sort operation, either ascending or descending.
 * Each constant carries a short abbreviation.
 */
@RequiredArgsConstructor
public enum SortOrder {

	/** Ascending sort order (smallest to largest). */
	ASCENDING("ASC"),
	/** Descending sort order (largest to smallest). */
	DESCENDING("DESC");

	@Getter private final @NotNull String shortName;

}
