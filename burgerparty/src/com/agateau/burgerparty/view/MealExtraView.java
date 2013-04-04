package com.agateau.burgerparty.view;

import java.util.HashSet;

import com.agateau.burgerparty.model.MealExtra;
import com.agateau.burgerparty.model.MealItem;
import com.agateau.burgerparty.utils.Signal0;
import com.agateau.burgerparty.utils.Signal1;
import com.agateau.burgerparty.utils.UiUtils;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;

public class MealExtraView extends Group {
	public MealExtraView(MealExtra mealExtra, TextureAtlas atlas) {
		mMealExtra = mealExtra;
		mAtlas = atlas;

		mMealExtra.itemAdded.connect(mHandlers, new Signal1.Handler<MealItem>() {
			public void handle(MealItem item) {
				addItem(item);
			}
		});

		mMealExtra.cleared.connect(mHandlers, new Signal0.Handler() {
			public void handle() {
				clearItems();
			}
		});
	}

	private void addItem(MealItem item) {
		TextureRegion region;
		region = mAtlas.findRegion("burgeritems-flat/" + item.getName());
		if (region == null) {
			region = mAtlas.findRegion("burgeritems/" + item.getName());
		}
		Image image = new Image(region);
		mImages.add(image);
		image.setPosition(MathUtils.ceil(getWidth()), ADD_ACTION_HEIGHT);
		addActor(image);
		image.addAction(Actions.alpha(0));
		image.addAction(Actions.parallel(
			Actions.moveBy(0, -ADD_ACTION_HEIGHT, MealView.ADD_ACTION_DURATION, Interpolation.pow2In),
			Actions.fadeIn(MealView.ADD_ACTION_DURATION)
			));

		updateGeometry();
	}
	
	private void clearItems() {
		mImages.clear();
		clear();
		updateGeometry();
	}

	private void updateGeometry() {
		if (mImages.size == 0) {
			setSize(0, 0);
			UiUtils.notifyResizeToFitParent(this);
			return;
		}
		float width = 0;
		float height = 0;
		for(Image image: mImages) {
			width += image.getWidth();
			height = Math.max(image.getHeight(), height);
		}
		setSize(width, height);
		UiUtils.notifyResizeToFitParent(this);
	}

	private MealExtra mMealExtra;
	private TextureAtlas mAtlas;
	private HashSet<Object> mHandlers = new HashSet<Object>();
	private Array<Image> mImages = new Array<Image>();

	private static final float ADD_ACTION_HEIGHT = 100;
}
