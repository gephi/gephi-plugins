package matties.plugin.HairballBuster;
/**
*© 2020 The Johns Hopkins University Applied Physics Laboratory LLC.
*NO WARRANTY, NO LIABILITY. THIS MATERIAL IS PROVIDED “AS IS.” JHU/APL MAKES NO
*REPRESENTATION OR WARRANTY WITH RESPECT TO THE PERFORMANCE OF THE MATERIALS,
*INCLUDING THEIR SAFETY, EFFECTIVENESS, OR COMMERCIAL VIABILITY, AND DISCLAIMS
*ALL WARRANTIES IN THE MATERIAL, WHETHER EXPRESS OR IMPLIED, INCLUDING (BUT NOT
*LIMITED TO) ANY AND ALL IMPLIED WARRANTIES OF PERFORMANCE, MERCHANTABILITY,
*FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT OF INTELLECTUAL PROPERTY
*OR OTHER THIRD PARTY RIGHTS. ANY USER OF THE MATERIAL ASSUMES THE ENTIRE RISK AND
*LIABILITY FOR USING THE MATERIAL. IN NO EVENT SHALL JHU/APL BE LIABLE TO ANY USER
*OF THE MATERIAL FOR ANY ACTUAL, INDIRECT, CONSEQUENTIAL, SPECIAL OR OTHER DAMAGES
*ARISING FROM THE USE OF, OR INABILITY TO USE, THE MATERIAL, INCLUDING, BUT NOT
*LIMITED TO, ANY DAMAGES FOR LOST PROFITS.
 */

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 * Implements <code>LayoutBuilder</code> class for Hairball Buster in Gephi
 */
@ServiceProvider(service = LayoutBuilder.class)
public class HairballBusterBuilder implements LayoutBuilder {
	
	private final HairballBusterUI ui = new HairballBusterUI();
	
    @Override
    public String getName() {
        return "Hairball Buster";
    }

    @Override
    public LayoutUI getUI() {
		return ui;
    }

    @Override
    public Layout buildLayout() {
        return new HairballBuster(this);
    }
	
	private class HairballBusterUI implements LayoutUI {

		@Override
		public String getDescription() {
			return "Plugin implementing Hairball Buster algorithm in Gephi";
		}

		@Override
		public Icon getIcon() {
			return null;
		}

		@Override
		public JPanel getSimplePanel(Layout layout) {
			return null;
		}

		@Override
		public int getQualityRank() {
			return 4;
		}

		@Override
		public int getSpeedRank() {
			return 4;
		}
		
	}
}