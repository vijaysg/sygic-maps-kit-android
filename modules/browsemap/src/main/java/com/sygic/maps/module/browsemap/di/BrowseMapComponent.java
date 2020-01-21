/*
 * Copyright (c) 2020 Sygic a.s. All rights reserved.
 *
 * This project is licensed under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.sygic.maps.module.browsemap.di;

import com.sygic.drive.module.networking.managers.auth.AuthManager;
import com.sygic.drive.module.networking.managers.networking.ApiRepository;
import com.sygic.maps.module.browsemap.BrowseMapFragment;
import com.sygic.maps.module.browsemap.di.module.BrowseMapModule;
import com.sygic.maps.module.browsemap.di.module.ViewModelModule;
import com.sygic.maps.module.common.di.FragmentModulesComponent;
import com.sygic.maps.module.common.di.base.BaseFragmentComponent;
import com.sygic.maps.module.common.di.util.ModuleBuilder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

import dagger.Component;

@Scope
@Retention(RetentionPolicy.RUNTIME)
@interface Browse { }

@Browse
@Component(
        modules = {
                ViewModelModule.class,
                BrowseMapModule.class
        },
        dependencies = {
                FragmentModulesComponent.class/*,
                NetworkingModulesComponent.class*/ //todo: instead use this
        }
)
public interface BrowseMapComponent extends BaseFragmentComponent<BrowseMapFragment> {
    @Component.Builder
    abstract class Builder implements ModuleBuilder<BrowseMapComponent> {}

    AuthManager getAuthManager(); //todo: not here
    ApiRepository getApiRepository(); //todo: not here
}