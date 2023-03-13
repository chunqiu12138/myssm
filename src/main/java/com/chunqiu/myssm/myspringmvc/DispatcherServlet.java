package com.chunqiu.myssm.myspringmvc;

import com.chunqiu.myssm.ioc.BeanFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@WebServlet("*.do")
public class DispatcherServlet extends ViewBaseServlet {
    private BeanFactory beanFactory;

    public void init() throws ServletException {
        super.init();
        //beanFactory = new ClassPathXmlApplicationContext();
        //优化为从application作用域去获取IOC容器
        beanFactory = (BeanFactory) getServletContext().getAttribute("beanFactory");
        if (beanFactory == null) throw new RuntimeException("IOC容器获取失败");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //根据url获取ServletPath
        String servletPath = req.getServletPath();
        //System.out.println(servletPath);
        servletPath = servletPath.substring(1, servletPath.length() - 3);
        //获取服务对应类对象
        Object controllerBeanObj = beanFactory.getBean(servletPath);
        //获取调方法名
        String operate = req.getParameter("operate");
        if(operate == null ) {
            operate = "index";
        }

        try {
            Method[] methods = controllerBeanObj.getClass().getDeclaredMethods();
            for (int k = 0; k < methods.length; k++) {
                if (methods[k].getName().equals(operate)) {
                    Method method = methods[k];
                    //1.统一获取请求参数
                    //获取当前方法的参数数组
                    Parameter[] methodParameters = method.getParameters();
                    Object[] parameterValues = new Object[methodParameters.length];
                    for (int i = 0; i < methodParameters.length; i++) {
                        Parameter parameter = methodParameters[i];
                        String parameterName = parameter.getName();
                        if ("req".equals(parameterName)) {
                            parameterValues[i] = req;
                        } else if("resp".equals(parameterName)) {
                            parameterValues[i] = resp;
                        } else if ("session".equals(parameterName)){
                            parameterValues[i] = req.getSession();
                        }else {
                            //从请求中获取参数值
                            String parameterValue = req.getParameter(parameterName);
                            //获取参数类型
                            String typeName = parameter.getType().getName();
                            Object parameterObj = parameterValue;
                            if ("java.lang.Integer".equals(typeName) && parameterObj != null) {
                                parameterObj = Integer.parseInt(parameterValue);
                            }
                            parameterValues[i] = parameterObj;
                        }
                    }

                    //2.controller方法调用
                    method.setAccessible(true);
                    Object returnObj = method.invoke(controllerBeanObj, parameterValues);

                    //3.视图处理
                    String methodReturnStr = (String) returnObj;
                    if (methodReturnStr.startsWith("redirect:")) {
                        String redirectStr = methodReturnStr.substring("redirect:".length());
                        resp.sendRedirect(redirectStr);
                    }else {
                        super.processTemplate(methodReturnStr, req, resp);
                    }
                }
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Dispatcher抛出异常");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
