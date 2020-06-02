package cn.edu.zust.dmt.hsy.my_base_library.containers.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import cn.edu.zust.dmt.hsy.my_annotations_library.annotations.MyRouter;
import cn.edu.zust.dmt.hsy.my_annotations_library.constants.MyRouterPaths;
import cn.edu.zust.dmt.hsy.my_base_library.helpers.MyErrorHelper;
import cn.edu.zust.dmt.hsy.my_base_library.helpers.MyRouterHelper;
import cn.edu.zust.dmt.hsy.my_base_library.interfaces.listeners.BaseViewModelListener;
import cn.edu.zust.dmt.hsy.my_base_library.interfaces.others.BaseExtrasListener;
import cn.edu.zust.dmt.hsy.my_base_library.viewmodels.BaseViewModel;

/**
 * @author MR.M
 * @version 1.0
 * @projectName TMS
 * @description $
 * @since 4/1/2020 20:16
 **/
public abstract class BaseActivity<T extends BaseViewModelListener, K extends BaseViewModel<T>>
        extends AppCompatActivity {

    private final ArrayList<BaseExtrasListener> mExtrasParserList = new ArrayList<>();

    private final K mViewModel;

    /**
     * @description check whether initialize {@link #mViewModel} or not
     */
    protected BaseActivity() {
        try {
            mViewModel = getViewModelClass().newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException("Failed on initialize ViewModel!");
        }
    }

    /**
     * @description inject {@link #getLayoutRId(),#findViews()}
     * inject methods provided by {@link #mExtrasParserList}
     */
    @Override
    protected final void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRId());
        findViews();
        loadActorsToViews();
        //set listener while views are created
        mViewModel.setCurrentListener(getViewModelListener());
        //trigger extras parser while views are created
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            for (BaseExtrasListener myExtrasParser : mExtrasParserList) {
                myExtrasParser.parseMyExtras(bundle);
            }
        }
    }

    /**
     * @description release all resources here to ensure {@link BaseActivity} could be finalized
     */
    @Override
    protected final void onDestroy() {
        super.onDestroy();
        mViewModel.onViewModelDestroyed();
    }

    /**
     * @description add BaseActivity Filter to {@link android.app.Activity#startActivity(Intent)}
     */
    @Override
    public final void startActivity(final Intent intent) {
        startActivity(intent, null);
    }

    /**
     * @description add BaseActivity Filter to {@link android.app.Activity#startActivity(Intent, Bundle)}
     */
    @Override
    public final void startActivity(final Intent intent, @Nullable final Bundle options) {
        try {
            final ComponentName componentName = intent.getComponent();
            if (componentName == null) {
                MyErrorHelper.showMyNullPointerException("TargetClass for this method is empty!");
            } else {
                if (BaseActivity.class.isAssignableFrom(Class.forName(componentName.getClassName()))) {
                    super.startActivity(intent, options);
                } else {
                    MyErrorHelper.showMyNullPointerException("Illegal targetClass for this method!");
                }
            }
        } catch (ClassNotFoundException e) {
            MyErrorHelper.showMyNullPointerException("TargetClass for this method not found!");
        }
    }

    /**
     * @param intent inject {@link #mExtrasParserList} methods
     */
    @Override
    protected final void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        final Bundle bundle = intent.getExtras();
        if (bundle != null) {
            for (BaseExtrasListener myExtrasParser : mExtrasParserList) {
                myExtrasParser.parseMyExtras(bundle);
            }
        }
    }

    /**
     * @param path     Path of targetClass annotated with {@link MyRouter}
     * @param myExtras extra info for path targetClass
     */
    protected final void callMyRouter(@NonNull final MyRouterPaths path, @Nullable final Bundle myExtras) {
        MyRouterHelper.INSTANCE.startBaseActivity(this, path, myExtras);
    }

    /**
     * @param myExtrasParser add new parser for myExtras
     */
    protected final void addMyExtrasParser(@NonNull final BaseExtrasListener myExtrasParser) {
        mExtrasParserList.add(myExtrasParser);
    }

    /**
     * @return {@link LayoutRes} contains view layout for {@link #setContentView(int)}
     */
    @LayoutRes
    protected abstract int getLayoutRId();

    /**
     * @return {@link BaseViewModel} for {@link BaseActivity}
     */
    @NonNull
    protected abstract Class<K> getViewModelClass();

    /**
     * @return {@link BaseViewModelListener} for {@link #getViewModelClass()}
     */
    @NonNull
    protected abstract T getViewModelListener();

    /**
     * @description find {@link View} from layout which needs add actions on
     */
    protected abstract void findViews();

    /**
     * @description load actors to views to ensure its motion attributes
     */
    protected abstract void loadActorsToViews();
}