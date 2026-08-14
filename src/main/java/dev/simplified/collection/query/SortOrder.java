package dev.simplified.collection.query;

import dev.simplified.annotations.Getter;
import dev.simplified.annotations.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the direction of a sort operation, either ascending or descending.
 * Each constant carries a short abbreviation.
 */
@RequiredArgsConstructor
public enum SortOrder {

	/**
	 * Ascending sort order (smallest to largest).
	 */
	ASCENDING("ASC"),
	/**
	 * Descending sort order (largest to smallest).
	 */
	DESCENDING("DESC");

	@Getter private final @NotNull String shortName;

}
