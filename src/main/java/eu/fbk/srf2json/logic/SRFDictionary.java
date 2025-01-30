package eu.fbk.srf2json.logic;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SRFDictionary {
	private class SRFDictionaryByPlantName {
		private class SRFDictionaryByLogicType {
			private class ClassLevelEntry {
				Map<String, String> aliases;
				Map<String, Integer> classIds;
				Map<String, Map<String, Integer>> attrIds;
				
				public ClassLevelEntry() {
					this.aliases = new CaseInsensitiveMap<>();
					this.classIds = new CaseInsensitiveMap<>();
					this.attrIds = new CaseInsensitiveMap<>();
				}
				
				public void addEntry(String className, Collection<String> aliases) {
					// Here we assume that no two classes have the same alias (otherwise one of the entries will be overridden)
					aliases.forEach(alias -> this.aliases.put(alias, className));
				}

				public void addIdInfo(String className, Integer classId, Map<String, Integer> attrIdMap) {
					classIds.put(lookup(className), classId);
					attrIds.put(lookup(className), new CaseInsensitiveMap<>(attrIdMap));
				}

				public Integer lookupClassId(String className) {
					return classIds.get(lookup(className));
				}

				public Map<String, Integer> getAttrIds(String className) {
					Map<String, Integer> res = attrIds.get(lookup(className));
					return res == null ? new HashMap<>() : new HashMap<>(res);
				}
				public String lookup(String alias) {
					String className = this.aliases.get(alias);

					return (className != null) ? className : alias;
				}

				@Override
				public String toString() {
					StringBuilder sb = new StringBuilder().append("[");
					this.aliases.forEach((alias, className) -> sb
						.append("\"")
						.append(alias)
						.append("->")
						.append(className)
						.append("\",")
					);
					this.classIds.forEach((className, classId) -> {
						sb
							.append(className)
							.append(":")
							.append(classId)
							.append("{")
						;
						Map<String, Integer> attrIdsMap = this.attrIds.get(className);
						if (attrIdsMap != null) {
							attrIdsMap.forEach((attrName, attrId) -> sb
								.append(attrName)
								.append(":")
								.append(attrId)
								.append(",")
							);
						}
						sb.append("}");
					});
					return sb.append("]").toString();
				}
			}
			
			Map<String, ClassLevelEntry> internalDictionary;
			
			public SRFDictionaryByLogicType() {
				this.internalDictionary = new HashMap<>();
				
				this.internalDictionary.put("LDS", new ClassLevelEntry());
				this.internalDictionary.put("LDV", new ClassLevelEntry());
			}
			
			public void addEntry(String logicType, String className, Collection<String> aliases) {
				ClassLevelEntry classLevelEntry = this.internalDictionary.get(logicType.toUpperCase());
				if (classLevelEntry == null) {
					throw new IllegalArgumentException("Unknown logic type: " + logicType);
				}
				
				classLevelEntry.addEntry(className, aliases);
			}

			public void addIdInfo(String logicType, String className, Integer classId, Map<String, Integer> attrIdMap) {
				ClassLevelEntry classLevelEntry = this.internalDictionary.get(logicType.toUpperCase());
				if (classLevelEntry == null) {
					throw new IllegalArgumentException("Unknown logic type: " + logicType);
				}

				classLevelEntry.addIdInfo(className, classId, attrIdMap);
			}

			public Integer lookupClassId(String logicType, String className) {
				ClassLevelEntry classLevelEntry = this.internalDictionary.get(logicType.toUpperCase());
				if (classLevelEntry == null) {
					throw new IllegalArgumentException("Unknown logic type: " + logicType);
				}

				return classLevelEntry.lookupClassId(className);
			}

			public Map<String, Integer> getAttrIds(String logicType, String className) {
				ClassLevelEntry classLevelEntry = this.internalDictionary.get(logicType.toUpperCase());
				if (classLevelEntry == null) {
					throw new IllegalArgumentException("Unknown logic type: " + logicType);
				}

				return classLevelEntry.getAttrIds(className);
			}

			public String lookup(String logicType, String alias) {
				ClassLevelEntry classLevelEntry = this.internalDictionary.get(logicType.toUpperCase());
				if (classLevelEntry == null) {
					throw new IllegalArgumentException("Unknown logic type: " + logicType);
				}
				
				return classLevelEntry.lookup(alias);
			}
			
			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder().append("{");
				this.internalDictionary.forEach((logicType, aliases) -> sb
					.append("\"")
					.append(logicType)
					.append("\":")
					.append(aliases.toString())
					.append(","));
				return sb.append("}").toString();
			}
		}
		
		Map<String, SRFDictionaryByLogicType> internalDictionary;
		
		public SRFDictionaryByPlantName() {
			this.internalDictionary = new CaseInsensitiveMap<>();
		}
		
		public void addEntry(String plantName, String logicType, String className, Collection<String> aliases) {
			SRFDictionaryByLogicType byLogicType = this.internalDictionary.get(plantName);
			if (byLogicType == null) {
				byLogicType = new SRFDictionaryByLogicType();
				this.internalDictionary.put(plantName, byLogicType);
			}
			
			byLogicType.addEntry(logicType, className, aliases);
		}

		public void addIdInfo(String plantName, String logicType, String className, Integer classId, Map<String, Integer> attrIdMap) {
			SRFDictionaryByLogicType byLogicType = this.internalDictionary.get(plantName);
			if (byLogicType == null) {
				throw new IllegalArgumentException("Plant name not found in the dictionary: " + plantName);
			}

			byLogicType.addIdInfo(logicType, className, classId, attrIdMap);
		}

		public Integer lookupClassId(String plantName, String logicType, String className) {
			SRFDictionaryByLogicType byLogicType = this.internalDictionary.get(plantName);
			if (byLogicType == null) {
				throw new IllegalArgumentException("Plant name not found in the dictionary: " + plantName);
			}

			return byLogicType.lookupClassId(logicType, className);
		}

		public Map<String, Integer> getAttrIds(String plantName, String logicType, String className) {
			SRFDictionaryByLogicType byLogicType = this.internalDictionary.get(plantName);
			if (byLogicType == null) {
				throw new IllegalArgumentException("Plant name not found in the dictionary: " + plantName);
			}

			return byLogicType.getAttrIds(logicType, className);
		}

		public String lookup(String plantName, String logicType, String alias) {
			SRFDictionaryByLogicType byLogicType = this.internalDictionary.get(plantName);
			if (byLogicType == null) {
				throw new IllegalArgumentException("Plant name not found in the dictionary: " + plantName);
			}
			
			return byLogicType.lookup(logicType, alias);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder().append("{");
			this.internalDictionary.forEach((plantName, byLogicType) -> sb
				.append("\"")
				.append(plantName)
				.append("\":")
				.append(byLogicType.toString())
				.append(","));
			return sb.append("}").toString();
		}
	}
	
	SRFDictionaryByPlantName byPlantName;
	
	public SRFDictionary() {
		this.byPlantName = new SRFDictionaryByPlantName();
	}
	
	public void addEntry(String plantName, String logicType, String className, Collection<String> aliases) {
		byPlantName.addEntry(plantName, logicType, className, aliases);
	}

	public void addIdInfo(String plantName, String logicType, String className, Integer classId, Map<String, Integer> attrIdMap) {
		byPlantName.addIdInfo(plantName, logicType, className, classId, attrIdMap);
	}

	public Integer lookupClassId(String plantName, String logicType, String className) {
		return byPlantName.lookupClassId(plantName, logicType, className);
	}

	public Map<String, Integer> getAttrIds(String plantName, String logicType, String className) {
		return byPlantName.getAttrIds(plantName, logicType, className);
	}

	public String lookup(String plantName, String logicType, String alias) {
		return byPlantName.lookup(plantName, logicType, alias);
	}

	public Set<String> getClassNames() {
		Set<String> res = new HashSet<>();
		
		this.byPlantName.internalDictionary.values().forEach(byLogicType ->
				byLogicType.internalDictionary.values().forEach(classLevelEntry ->
						res.addAll(classLevelEntry.aliases.values())
				)
		);
		
		return res;
	}

	public Set<Integer> getAssignedClassIds() {
		Set<Integer> res = new HashSet<>();

		this.byPlantName.internalDictionary.values().forEach(byLogicType ->
				byLogicType.internalDictionary.values().forEach(classLevelEntry ->
						res.addAll(classLevelEntry.classIds.values())
				)
		);

		return res;
	}
	
	@Override
	public String toString() {
		return "{\"SRFDictionary\":" +
				this.byPlantName.toString() +
				"}"
		;
	}
}
