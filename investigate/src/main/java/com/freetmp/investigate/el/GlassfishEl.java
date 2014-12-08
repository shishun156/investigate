package com.freetmp.investigate.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;
import javax.el.ValueExpression;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.sun.el.ExpressionFactoryImpl;

/**
 * 测试单独使用el表达式
 * @author pin
 */
public class GlassfishEl {
	
	public static class Persion{
		String name;
		String city;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
	}
	
	public static class MyELResolver extends ELResolver {

		private Persion persion;
		
		public MyELResolver(Persion persion) {
			super();
			this.persion = persion;
		}

		@Override
		public Object getValue(ELContext context, Object base, Object property) {
			if(base == null){
				try {
					Object value = FieldUtils.readDeclaredField(persion, (String)property,true);
					if(value != null) context.setPropertyResolved(true);
					return value;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public Class<?> getType(ELContext context, Object base, Object property) {
			if(base == null){
				return FieldUtils.getDeclaredField(Persion.class, (String)property).getType();
			}
			return null;
		}

		@Override
		public void setValue(ELContext context, Object base, Object property, Object value) {
			
		}

		@Override
		public boolean isReadOnly(ELContext context, Object base, Object property) {
			return false;
		}

		@Override
		public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
			return null;
		}

		@Override
		public Class<?> getCommonPropertyType(ELContext context, Object base) {
			return null;
		}
		
	}

	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
		ExpressionFactory factory = new ExpressionFactoryImpl();
		StandardELContext context = new StandardELContext(factory);

		context.getFunctionMapper().mapFunction("math", "max", 
				Math.class.getMethod("max", int.class,int.class));
		
		ValueExpression mathMax = factory.createValueExpression(context, "${math:max(foo,bar)}", int.class);
		factory.createValueExpression(context, "${bar}", int.class)
			   .setValue(context, 1);
		factory.createValueExpression(context, "${foo}", int.class)
		   .setValue(context, 2);
		
		System.out.println(mathMax.getValue(context));
		
		Persion persion = new Persion();
		persion.city = "beijing";
		persion.name = "John";
		context.getVariableMapper().setVariable("persion", factory.createValueExpression(persion, Persion.class));
		
		ValueExpression object = factory.createValueExpression(context, "${persion.city}",String.class);
		System.out.println(object.getValue(context));
		
		//测试lambda表达式
		ValueExpression lambda = factory.createValueExpression(context, "${((x,y)->x+y)(3,4)}", Object.class);
		System.out.println(lambda.getValue(context));
		
		//测试集合创建
		ValueExpression list = factory.createValueExpression(context, "${[1,\"two\",['three','four']]}",List.class);
		System.out.println(list.getValue(context));
		
		//测试映射创建
		ValueExpression map = factory.createValueExpression(context, "${{\"one\":1, \"two\":2, \"three\":3}}", Map.class);
		System.out.println(map.getValue(context));
		
		//测试自定义的解析器
		context.addELResolver(new MyELResolver(persion));
		ValueExpression city = factory.createValueExpression(context, "${city}",String.class);
		System.out.println(city.getValue(context));
	}
}
