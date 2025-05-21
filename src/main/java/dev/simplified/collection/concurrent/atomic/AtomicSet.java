package dev.sbs.api.collection.concurrent.atomic;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Set;

public abstract class AtomicSet<E, T extends AbstractSet<E>> extends AtomicCollection<E, T> implements Set<E> {

	protected AtomicSet(@NotNull T type) {
		super(type);
	}

}
