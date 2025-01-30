package eu.fbk.srf2json.logic;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import eu.fbk.srf2json.dataclasses.ClassDC;

import org.apache.commons.collections4.iterators.IteratorChain;

public class ClassIdContainer implements Iterable<ClassDC> {
	
	public class ClassIdContainerByLogicType implements Iterable<ClassDC> {

		public class ClassIdContainerByPlant implements Iterable<ClassDC> {

			public class ClassIdContainerByClassDC implements Iterable<ClassDC> {
				private SortedSet<ClassDC> classes;

				public ClassIdContainerByClassDC() {
					this.classes = new TreeSet<>((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
				}

				@Override
				public Iterator<ClassDC> iterator() {
					return this.classes.iterator();
				}

				public void addClass(ClassDC classDC) {
					this.classes.add(classDC);
				}
			}
				
			private SortedMap<String, ClassIdContainerByClassDC>  plants;

			public ClassIdContainerByPlant() {
				this.plants = new TreeMap<>();
			}

			@Override
			public Iterator<ClassDC> iterator() {
				return new IteratorChain<>(
						this.plants.values().stream().map(container -> container.iterator()).collect(Collectors.toList())
				);
			}

			public ClassIdContainerByClassDC getPlantEntry(String plantName) {
				ClassIdContainerByClassDC res = this.plants.get(plantName);
				if (res == null) {
					res = new ClassIdContainerByClassDC();
					this.plants.put(plantName, res);
				}

				return res;
			}
		}
		
		private ClassIdContainerByPlant LdS, LdV;

		public ClassIdContainerByLogicType() {
			this.LdS = new ClassIdContainerByPlant();
			this.LdV = new ClassIdContainerByPlant();
		}
		
		@Override
		public Iterator<ClassDC> iterator() {
			return new IteratorChain<>(
					LdS.iterator(),
					LdV.iterator()
			);
		}

		public ClassIdContainerByPlant getLogicType(boolean isLdS) {
			return isLdS ? LdS : LdV;
		}
	}
	
	private ClassIdContainerByLogicType upperLevel;
	
	public ClassIdContainer() {
		super();
		
		upperLevel = new ClassIdContainerByLogicType();
	}

	public void registerClass(ClassDC classDC, String plantName, boolean isLdS) {
		upperLevel
			.getLogicType(isLdS)
			.getPlantEntry(plantName)
			.addClass(classDC);
	}
	
	@Override
	public Iterator<ClassDC> iterator() {
		return upperLevel.iterator();
	}
}
