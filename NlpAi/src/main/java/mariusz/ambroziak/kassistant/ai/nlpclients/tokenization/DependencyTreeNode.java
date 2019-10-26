package mariusz.ambroziak.kassistant.ai.nlpclients.tokenization;

import java.util.List;

public class DependencyTreeNode {

		private String text;
		private String relationToParent;
		private String pos;
		private List<DependencyTreeNode> children;
		
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getRelationToParent() {
			return relationToParent;
		}
		public void setRelationToParent(String relationToParent) {
			this.relationToParent = relationToParent;
		}
		public String getPos() {
			return pos;
		}
		public void setPos(String pos) {
			this.pos = pos;
		}
		public List<DependencyTreeNode> getChildren() {
			return children;
		}
		public void setChildren(List<DependencyTreeNode> children) {
			this.children = children;
		}
		
		
		
		
		
}
