package com.mnao.mfp.common.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.validation.constraints.NotNull;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

public class NullCheck<T> {
	
	private final T root;
	
	public NullCheck(@Nullable T root) {
		this.root = root;
	}
	
	@Nullable
	public T get() {
		return this.root;
	}
	
	public boolean isNotNull() {
		return this.root != null;
	}
	
	public boolean isNull() {
		return this.root == null;
	}
	
	public boolean isNotNullOrEmpty() {
		return (this.root != null && !"".equals(this.root));
	}
	
	public T orElseGet(Supplier<? extends T> other) {
		return this.root != null ? this.root : other.get();
	}
	
	public T orElse(@NotNull T value) {
		return this.root != null ? this.root : value;
	}
	
	public <C> NullCheck<C> with(Function<T, C> getter) {
		return root != null ? new NullCheck<>(getter.apply(root)) : new NullCheck<>(null);
	}
	
	@NotNull
	public <C> NullCheck<C> withEmpty(Function<T, C> getter) {
		return (root != null && !StringUtils.isEmpty(root)) ? new NullCheck<>(getter.apply(root)) : new NullCheck<>(null);
	}
	
	@SafeVarargs
	public final <V> NullCheck<V> allNotNull(@Nullable final V... objects) {
		if (objects != null) {
			for (final V val: objects) {
				if (val == null) {
					return new NullCheck<>(null);
				}
			}
			return new NullCheck<>(objects[0]);
		}
		return new NullCheck<>(null);
	}

	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (root != null) {
			return root;
		} else {
			throw exceptionSupplier.get();
		}
	}
	
	public boolean equalsIgnoreCase(@NotNull String otherString) {
		if (root != null) {
			String rootString = (String) this.root;
			return rootString.startsWith(otherString);
		}
		return false;
	}
	
	public boolean startsWith(@NotNull String otherString) {
		return root != null && otherString.equalsIgnoreCase((String) root);
	}
	
	public boolean deepEquals(@NotNull T otherType) {
		return Objects.deepEquals(root, otherType);
	}
	
	
}
