package com.aol.cyclops.matcher.builders;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

import com.aol.cyclops.matcher.PatternMatcher;
@AllArgsConstructor
public class MatchingInstance <T, X> implements Function<T, Optional<X>> {
	
	private final Case cse;
	
	public final StreamCase streamCase(){
		StreamCase cse = new StreamCase(new PatternMatcher());
		return cse;
	}
	public final AggregatedCase<X> newCase(){
		AggregatedCase<X> cse = new AggregatedCase<X>(new PatternMatcher());
		return cse;
	}
	public final AtomisedCase atomisedCase(){
		AtomisedCase cse = new AtomisedCase(new PatternMatcher());
		return cse;
	}
	
	
	public final MatchingInstance<T,X> streamCase(Consumer<Case> consumer){
		StreamCase cse = new StreamCase(new PatternMatcher());
		consumer.accept(cse);
		return this;
	}
	public final MatchingInstance<T,X> simpleCase(Consumer<AggregatedCase<X>> consumer){
		AggregatedCase<X> cse = new AggregatedCase<>(new PatternMatcher());
		consumer.accept(cse);
		return this;
	}
	public final MatchingInstance<T,X> atomisedCase(Consumer<AtomisedCase> consumer){
		AtomisedCase cse = new AtomisedCase(new PatternMatcher());
		consumer.accept(cse);
		return this;
	}
	
	public Function<T,X> asUnwrappedFunction(){
		return cse.getPatternMatcher().asUnwrappedFunction();
	}
	
	public Function<T,Stream<X>> asStreamFunction(){
		
		return	cse.getPatternMatcher().asStreamFunction();
	}
	/* 
	 *	@param t Object to match against
	 *	@return Value from matched case if present
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	public Optional<X> apply(Object t){
		return (Optional<X>)cse.getPatternMatcher().apply(t);
	}
	
	/**
	 * Each input element can generated multiple matched values
	 * 
	 * @param s  Stream of data to match against (input to matcher)
	 * @return Stream of values from matched cases
	 */
	public<R> Stream<R> matchManyFromStream(Stream s){
		return cse.getPatternMatcher().matchManyFromStream(s);
	}
	
	/**
	 * 
	 * @param t input to match against - can generate multiple values
	 * @return Stream of values from matched cases for the input
	 */
	public<R> Stream<R> matchMany(Object t) {
		return cse.getPatternMatcher().matchMany(t);
	}
	
	/**
	 * Each input element can generated a single matched value
	 * 
	 * @param s Stream of data to match against (input to matcher)
	 * @return Stream of matched values, one case per input value can match
	 */
	public <R> Stream<R> matchFromStream(Stream s){
		
		return cse.getPatternMatcher().matchFromStream(s);
	}
	public <R> Optional<R> match(Object... t){
		return cse.getPatternMatcher().match(t);
	}
	/**
	 * @param t Object to match against supplied cases
	 * @return Value returned from matched case (if present) otherwise Optional.empty()
	 */
	public  Optional<X> match(Object t){
		return cse.getPatternMatcher().match(t);
	}
}
