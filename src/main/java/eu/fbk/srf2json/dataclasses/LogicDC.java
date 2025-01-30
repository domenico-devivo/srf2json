package eu.fbk.srf2json.dataclasses;

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "classes" })
public class LogicDC extends ClassContainerDC {
		private String tag;
		
		public LogicDC(LogicTypeDC logicType) {
			super();
		
			this.tag = "Logic";
			
			this.logicType = logicType;
			logicType.setLogic(this);
		}

		@Override
		public LogicDC addClass(ClassDC classDC) {
			super.addClass(classDC);
			return this;
		}
		
		@Override
		public LogicDC addClasses(Collection<ClassDC> classes) {
			super.addClasses(classes);
			return this;
		}
		
		@Override
		public boolean isLogic() {
			return true;
		}
}
